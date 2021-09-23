/*******************************************************************************
 * Copyright (C) 2007, 2015:
 * 
 * - Ferdinando Villa <ferdinando.villa@bc3research.org> - integratedmodelling.org - any other
 * authors listed in @author annotations
 *
 * All rights reserved. This file is part of the k.LAB software suite, meant to enable modular,
 * collaborative, integrated development of interoperable data and model components. For details,
 * see http://integratedmodelling.org.
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the
 * Affero General Public License Version 3 or any later version.
 *
 * This program is distributed in the hope that it will be useful, but without any warranty; without
 * even the implied warranty of merchantability or fitness for a particular purpose. See the Affero
 * General Public License for more details.
 * 
 * You should have received a copy of the Affero General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. The license is also available at: https://www.gnu.org/licenses/agpl.html
 *******************************************************************************/
package org.integratedmodelling.klab.owl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.integratedmodelling.kim.api.IKimConcept.Type;
import org.integratedmodelling.kim.api.IKimNamespace;
import org.integratedmodelling.klab.Authorities;
import org.integratedmodelling.klab.Namespaces;
import org.integratedmodelling.klab.api.knowledge.IAuthority;
import org.integratedmodelling.klab.api.knowledge.IAxiom;
import org.integratedmodelling.klab.api.knowledge.IConcept;
import org.integratedmodelling.klab.api.knowledge.IMetadata;
import org.integratedmodelling.klab.api.knowledge.IOntology;
import org.integratedmodelling.klab.api.knowledge.IProperty;
import org.integratedmodelling.klab.api.model.INamespace;
import org.integratedmodelling.klab.common.SemanticType;
import org.integratedmodelling.klab.data.Metadata;
import org.integratedmodelling.klab.engine.resources.CoreOntology.NS;
import org.integratedmodelling.klab.exceptions.KlabException;
import org.integratedmodelling.klab.exceptions.KlabIOException;
import org.integratedmodelling.klab.exceptions.KlabInternalErrorException;
import org.integratedmodelling.klab.exceptions.KlabValidationException;
import org.integratedmodelling.klab.utils.MiscUtilities;
import org.integratedmodelling.klab.utils.StringUtil;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLProperty;

/**
 * A proxy for an ontology. Holds a list of concepts and a list of axioms. Can be turned into a list
 * and marshalled to a server for actual knowledge creation. Contains no instances, properties or
 * restrictions directly, just concepts for indexing and axioms for the actual stuff.
 * 
 * @author Ferd
 */
public class Ontology implements IOntology {

    String                       id;
    private Set<String>          imported      = new HashSet<>();
    private Map<String, Concept> delegates     = new HashMap<>();
    OWLOntology                  ontology;
    private String               prefix;
    private Map<String, Concept> conceptIDs    = new HashMap<>();
    Map<String, Individual>      individuals   = new HashMap<>();

    /*
     * all properties
     */
    Set<String>                  propertyIDs   = new HashSet<>();

    /*
     * property IDs by class - no other way to return the OWL objects quickly. what a pain
     */
    Set<String>                  opropertyIDs  = new HashSet<>();
    Set<String>                  dpropertyIDs  = new HashSet<>();
    Set<String>                  apropertyIDs  = new HashSet<>();
    Map<String, String>          definitionIds = Collections.synchronizedMap(new HashMap<>());

    private String               resourceUrl;
    private boolean              isInternal    = false;
    private AtomicInteger        idCounter     = new AtomicInteger(0);

    Ontology(OWLOntology ontology, String id) {

        this.id = id;
        this.ontology = ontology;
        this.prefix = ontology.getOntologyID().getOntologyIRI().toString();

        /**
         * FIXME can't spend a week figuring this out, but this is NOT nice.
         */
        if (this.prefix.equals("http://purl.obolibrary.org/obo/bfo.owl")) {
            this.prefix = "http://purl.obolibrary.org/obo";
        }
        
        scan();
    }

    public String getPrefix() {
        return this.prefix;
    }

    /*
     * build a catalog of names, as there seems to be no way to quickly assess if an ontology contains
     * a named entity or not. This needs to be kept in sync with any changes, which is a pain. FIXME
     * reintegrate the conditionals when define() works properly.
     */
    private void scan() {

        for (OWLClass c : this.ontology.getClassesInSignature(false)) {
            if (c.getIRI().toString().contains(this.prefix)
                    && !this.conceptIDs.containsKey(c.getIRI().getFragment())) {
                this.conceptIDs.put(c.getIRI().getFragment(), new Concept(c, this.id, OWL.emptyType));
            }
        }
        for (OWLProperty<?, ?> p : this.ontology.getDataPropertiesInSignature(false)) {
            if (p.getIRI().toString().contains(this.prefix)) {
                this.dpropertyIDs.add(p.getIRI().getFragment());
                this.propertyIDs.add(p.getIRI().getFragment());
            }
        }
        for (OWLProperty<?, ?> p : this.ontology.getObjectPropertiesInSignature(false)) {
            if (p.getIRI().toString().contains(this.prefix)) {
                this.opropertyIDs.add(p.getIRI().getFragment());
                this.propertyIDs.add(p.getIRI().getFragment());
            }
        }
        for (OWLAnnotationProperty p : this.ontology.getAnnotationPropertiesInSignature()) {
            if (p.getIRI().toString().contains(this.prefix)) {
                this.apropertyIDs.add(p.getIRI().getFragment());
                this.propertyIDs.add(p.getIRI().getFragment());
            }
        }
    }

    public void addDelegateConcept(String id, IKimNamespace namespace, Concept concept) {
        this.delegates.put(id, wrapDelegate(concept, id));
        concept.getOntology().define(Collections.singleton(Axiom.AnnotationAssertion(concept
                .getName(), NS.LOCAL_ALIAS_PROPERTY, namespace.getName() + ":" + id)));
    }

    private Concept wrapDelegate(Concept concept, String id) {
        Concept ret = new Concept(concept);
        ret.setMetadata(new Metadata());
        ret.getMetadata().putAll(concept.getMetadata());
        ret.getMetadata().put(NS.REFERENCE_NAME_PROPERTY, KimKnowledgeProcessor.getCleanFullId(this.id, id));
        return ret;
    }

    @Override
    public Collection<IConcept> getConcepts() {

        ArrayList<IConcept> ret = new ArrayList<>(conceptIDs.values());
        // for (String s : this.conceptIDs) {
        // ret.add(getConcept(s));
        // }
        return ret;
    }

    @Override
    public Collection<IProperty> getProperties() {
        ArrayList<IProperty> ret = new ArrayList<>();
        for (OWLProperty<?, ?> p : this.ontology.getDataPropertiesInSignature(false)) {
            ret.add(new Property(p, this.id));
        }
        for (OWLProperty<?, ?> p : this.ontology.getObjectPropertiesInSignature(false)) {
            ret.add(new Property(p, this.id));
        }
        for (OWLAnnotationProperty p : this.ontology.getAnnotationPropertiesInSignature()) {
            ret.add(new Property(p, this.id));
        }

        return ret;
    }

    @Override
    public Concept getConcept(String ID) {
        Concept ret = this.conceptIDs.get(ID);
        if (ret != null) {
            return ret;
        }
        return delegates.get(ID);
    }

    @Override
    public Property getProperty(String ID) {
        if (this.opropertyIDs.contains(ID)) {
            return new Property(this.ontology.getOWLOntologyManager().getOWLDataFactory()
                    .getOWLObjectProperty(IRI.create(this.prefix + "#" + ID)), this.id);
        }
        if (this.dpropertyIDs.contains(ID)) {
            return new Property(this.ontology.getOWLOntologyManager().getOWLDataFactory()
                    .getOWLDataProperty(IRI.create(this.prefix + "#" + ID)), this.id);
        }
        if (this.apropertyIDs.contains(ID)) {
            return new Property(this.ontology.getOWLOntologyManager().getOWLDataFactory()
                    .getOWLAnnotationProperty(IRI.create(this.prefix + "#" + ID)), this.id);
        }
        return null;
    }

    @Override
    public String getURI() {
        return this.ontology.getOWLOntologyManager().getOntologyDocumentIRI(this.ontology).toString();
    }

    @Override
    public boolean write(File file, boolean writeImported) throws KlabException {

        if (writeImported) {

            Set<IOntology> authorities = new HashSet<>(getDelegateOntologies());

            File path = MiscUtilities.getPath(file.toString());
            String myns = this.ontology.getOntologyID().getOntologyIRI().getNamespace();
            for (OWLOntology o : this.ontology.getImportsClosure()) {
                String iri = o.getOntologyID().getOntologyIRI().toString();
                if (iri.startsWith(myns) && !o.equals(this.ontology)) {
                    String fr = o.getOntologyID().getOntologyIRI().getFragment();
                    INamespace other = Namespaces.INSTANCE.getNamespace(fr);
                    if (other != null) {
                        if (!fr.endsWith(".owl")) {
                            fr += ".owl";
                        }
                        File efile = new File((path.toString().equals(".") ? "" : (path + File.separator))
                                + fr);
                        other.getOntology().write(efile, false);
                        authorities.addAll(((Ontology) other.getOntology()).getDelegateOntologies());
                    }
                }
            }

            for (IOntology o : authorities) {
                File efile = new File((path.toString().equals(".") ? "" : (path + File.separator))
                        + o.getName() + ".owl");
                o.write(efile, false);
            }
        }

        OWLOntologyFormat format = this.ontology.getOWLOntologyManager().getOntologyFormat(this.ontology);
        OWLXMLOntologyFormat owlxmlFormat = new OWLXMLOntologyFormat();

        if (format.isPrefixOWLOntologyFormat()) {
            owlxmlFormat.copyPrefixesFrom(format.asPrefixOWLOntologyFormat());
        }
        try {
            this.ontology.getOWLOntologyManager()
                    .saveOntology(this.ontology, owlxmlFormat, IRI.create(file.toURI()));
        } catch (OWLOntologyStorageException e) {
            throw new KlabIOException(e);
        }

        return true;
    }

    /**
     * Return the ontologies that host all authority concepts we delegate to.
     * 
     * @return the delegate ontologies
     */
    public Collection<IOntology> getDelegateOntologies() {
        Set<IOntology> ret = new HashSet<>();
        for (IConcept c : delegates.values()) {
            ret.add(c.getOntology());
        }
        return ret;
    }

    @Override
    public Collection<String> define(Collection<IAxiom> axioms) {

        ArrayList<String> errors = new ArrayList<>();

        /*
         * ACHTUNG remember to add IDs to appropriate catalogs as classes and property assertions are
         * encountered. This can be called incrementally, so better not to call scan() every time.
         */
        OWLDataFactory factory = this.ontology.getOWLOntologyManager().getOWLDataFactory();

        for (IAxiom axiom : axioms) {

//             System.out.println(" [" + id + "] => " + axiom);

            try {

                if (axiom.is(IAxiom.CLASS_ASSERTION)) {

                    OWLClass newcl = factory
                            .getOWLClass(IRI.create(this.prefix + "#" + axiom.getArgument(0)));
                    this.ontology.getOWLOntologyManager()
                            .addAxiom(this.ontology, factory.getOWLDeclarationAxiom(newcl));
                    this.conceptIDs.put(axiom.getArgument(0)
                            .toString(), new Concept(newcl, id, ((Axiom) axiom).conceptType));

                } else if (axiom.is(IAxiom.SUBCLASS_OF)) {

                    OWLClass subclass = findClass(axiom.getArgument(1).toString(), errors);
                    OWLClass superclass = findClass(axiom.getArgument(0).toString(), errors);

                    if (subclass != null && superclass != null) {
                        OWL.INSTANCE.manager
                                .addAxiom(this.ontology, factory.getOWLSubClassOfAxiom(subclass, superclass));
                    }

                } else if (axiom.is(IAxiom.ANNOTATION_PROPERTY_ASSERTION)) {

                    OWLAnnotationProperty p = factory
                            .getOWLAnnotationProperty(IRI.create(this.prefix + "#" + axiom.getArgument(0)));
                    // this.ontology.getOWLOntologyManager().addAxiom(this.ontology,
                    // factory.getOWLDeclarationAxiom(p));
                    this.propertyIDs.add(axiom.getArgument(0).toString());
                    this.apropertyIDs.add(axiom.getArgument(0).toString());
                    OWLMetadata._metadataVocabulary
                            .put(p.getIRI().toString(), getName() + ":" + axiom.getArgument(0));

                } else if (axiom.is(IAxiom.DATA_PROPERTY_ASSERTION)) {

                    OWLDataProperty p = factory
                            .getOWLDataProperty(IRI.create(this.prefix + "#" + axiom.getArgument(0)));
                    this.ontology.getOWLOntologyManager()
                            .addAxiom(this.ontology, factory.getOWLDeclarationAxiom(p));
                    this.propertyIDs.add(axiom.getArgument(0).toString());
                    this.dpropertyIDs.add(axiom.getArgument(0).toString());

                } else if (axiom.is(IAxiom.DATA_PROPERTY_DOMAIN)) {

                    OWLEntity property = findProperty(axiom.getArgument(0).toString(), true, errors);
                    OWLClass classExp = findClass(axiom.getArgument(1).toString(), errors);

                    if (property != null && classExp != null) {
                        OWL.INSTANCE.manager.addAxiom(this.ontology, factory
                                .getOWLDataPropertyDomainAxiom(property.asOWLDataProperty(), classExp));
                    }

                } else if (axiom.is(IAxiom.DATA_PROPERTY_RANGE)) {

                    OWLEntity property = findProperty(axiom.getArgument(0).toString(), true, errors);
                    /*
                     * TODO XSD stuff
                     */

                    // _manager.manager.addAxiom(
                    // _ontology,
                    // factory.getOWLDataPropertyRangeAxiom(property.asOWLDataProperty(),
                    // classExp));

                } else if (axiom.is(IAxiom.OBJECT_PROPERTY_ASSERTION)) {

                    OWLObjectProperty p = factory
                            .getOWLObjectProperty(IRI.create(this.prefix + "#" + axiom.getArgument(0)));
                    this.ontology.getOWLOntologyManager()
                            .addAxiom(this.ontology, factory.getOWLDeclarationAxiom(p));
                    this.propertyIDs.add(axiom.getArgument(0).toString());
                    this.opropertyIDs.add(axiom.getArgument(0).toString());

                } else if (axiom.is(IAxiom.OBJECT_PROPERTY_DOMAIN)) {

                    OWLEntity property = findProperty(axiom.getArgument(0).toString(), false, errors);
                    OWLClass classExp = findClass(axiom.getArgument(1).toString(), errors);

                    if (property != null && classExp != null) {
                        OWL.INSTANCE.manager.addAxiom(this.ontology, factory
                                .getOWLObjectPropertyDomainAxiom(property.asOWLObjectProperty(), classExp));
                    }

                } else if (axiom.is(IAxiom.OBJECT_PROPERTY_RANGE)) {

                    OWLEntity property = findProperty(axiom.getArgument(0).toString(), false, errors);
                    OWLClass classExp = findClass(axiom.getArgument(1).toString(), errors);

                    if (property != null && classExp != null) {
                        OWL.INSTANCE.manager.addAxiom(this.ontology, factory
                                .getOWLObjectPropertyRangeAxiom(property.asOWLObjectProperty(), classExp));
                    }

                } else if (axiom.is(IAxiom.ALL_VALUES_FROM_RESTRICTION)) {

                    OWLEntity property = findProperty(axiom.getArgument(1).toString(), false, errors);
                    OWLClass target = findClass(axiom.getArgument(0).toString(), errors);
                    OWLClass filler = findClass(axiom.getArgument(2).toString(), errors);
                    OWLClassExpression restr = factory
                            .getOWLObjectAllValuesFrom(property.asOWLObjectProperty(), filler);

                    if (property != null && filler != null && target != null && restr != null) {
                        OWL.INSTANCE.manager
                                .addAxiom(this.ontology, factory.getOWLSubClassOfAxiom(target, restr));
                    }

                } else if (axiom.is(IAxiom.AT_LEAST_N_VALUES_FROM_RESTRICTION)) {

                    int n = ((Number) axiom.getArgument(3)).intValue();

                    OWLEntity property = findProperty(axiom.getArgument(1).toString(), false, errors);
                    OWLClass target = findClass(axiom.getArgument(0).toString(), errors);
                    OWLClass filler = findClass(axiom.getArgument(2).toString(), errors);

                    OWLClassExpression restr = factory
                            .getOWLObjectMinCardinality(n, property.asOWLObjectProperty(), filler);

                    if (property != null && filler != null && target != null && restr != null) {
                        OWL.INSTANCE.manager
                                .addAxiom(this.ontology, factory.getOWLSubClassOfAxiom(target, restr));
                    }

                } else if (axiom.is(IAxiom.AT_MOST_N_VALUES_FROM_RESTRICTION)) {

                    int n = ((Number) axiom.getArgument(3)).intValue();

                    OWLEntity property = findProperty(axiom.getArgument(1).toString(), false, errors);
                    OWLClass target = findClass(axiom.getArgument(0).toString(), errors);
                    OWLClass filler = findClass(axiom.getArgument(2).toString(), errors);

                    OWLClassExpression restr = factory
                            .getOWLObjectMaxCardinality(n, property.asOWLObjectProperty(), filler);

                    if (property != null && filler != null && target != null && restr != null) {
                        OWL.INSTANCE.manager
                                .addAxiom(this.ontology, factory.getOWLSubClassOfAxiom(target, restr));
                    }

                } else if (axiom.is(IAxiom.EXACTLY_N_VALUES_FROM_RESTRICTION)) {

                    int n = ((Number) axiom.getArgument(3)).intValue();

                    OWLEntity property = findProperty(axiom.getArgument(1).toString(), false, errors);
                    OWLClass target = findClass(axiom.getArgument(0).toString(), errors);
                    OWLClass filler = findClass(axiom.getArgument(2).toString(), errors);

                    OWLClassExpression restr = factory
                            .getOWLObjectExactCardinality(n, property.asOWLObjectProperty(), filler);

                    if (property != null && filler != null && target != null && restr != null) {
                        OWL.INSTANCE.manager
                                .addAxiom(this.ontology, factory.getOWLSubClassOfAxiom(target, restr));
                    }

                } else if (axiom.is(IAxiom.SOME_VALUES_FROM_RESTRICTION)) {

                    OWLEntity property = findProperty(axiom.getArgument(1).toString(), false, errors);
                    OWLClass target = findClass(axiom.getArgument(0).toString(), errors);
                    OWLClass filler = findClass(axiom.getArgument(2).toString(), errors);

                    OWLClassExpression restr = factory
                            .getOWLObjectSomeValuesFrom(property.asOWLObjectProperty(), filler);

                    if (property != null && filler != null && target != null && restr != null) {
                        OWL.INSTANCE.manager
                                .addAxiom(this.ontology, factory.getOWLSubClassOfAxiom(target, restr));
                    }

                } else if (axiom.is(IAxiom.DATATYPE_DEFINITION)) {

                } else if (axiom.is(IAxiom.DISJOINT_CLASSES)) {

                    Set<OWLClassExpression> classExpressions = new HashSet<>();
                    for (Object arg : axiom) {
                        OWLClass p = factory.getOWLClass(IRI.create(this.prefix + "#" + arg));
                        classExpressions.add(p);
                    }
                    OWL.INSTANCE.manager
                            .addAxiom(this.ontology, factory.getOWLDisjointClassesAxiom(classExpressions));

                } else if (axiom.is(IAxiom.ASYMMETRIC_OBJECT_PROPERTY)) {

                } else if (axiom.is(IAxiom.DIFFERENT_INDIVIDUALS)) {

                } else if (axiom.is(IAxiom.DISJOINT_OBJECT_PROPERTIES)) {

                } else if (axiom.is(IAxiom.DISJOINT_DATA_PROPERTIES)) {

                } else if (axiom.is(IAxiom.DISJOINT_UNION)) {

                } else if (axiom.is(IAxiom.EQUIVALENT_CLASSES)) {

                    Set<OWLClassExpression> classExpressions = new HashSet<>();
                    for (Object arg : axiom) {
                        OWLClass classExp = findClass(arg.toString(), errors);
                        classExpressions.add(classExp);
                    }
                    OWL.INSTANCE.manager
                            .addAxiom(this.ontology, factory.getOWLEquivalentClassesAxiom(classExpressions));

                } else if (axiom.is(IAxiom.EQUIVALENT_DATA_PROPERTIES)) {

                } else if (axiom.is(IAxiom.EQUIVALENT_OBJECT_PROPERTIES)) {

                } else if (axiom.is(IAxiom.FUNCTIONAL_DATA_PROPERTY)) {

                    OWLDataProperty prop = factory
                            .getOWLDataProperty(IRI.create(this.prefix + "#" + axiom.getArgument(0)));
                    OWL.INSTANCE.manager
                            .addAxiom(this.ontology, factory.getOWLFunctionalDataPropertyAxiom(prop));

                } else if (axiom.is(IAxiom.FUNCTIONAL_OBJECT_PROPERTY)) {

                    OWLObjectProperty prop = factory
                            .getOWLObjectProperty(IRI.create(this.prefix + "#" + axiom.getArgument(0)));
                    OWL.INSTANCE.manager
                            .addAxiom(this.ontology, factory.getOWLFunctionalObjectPropertyAxiom(prop));

                } else if (axiom.is(IAxiom.INVERSE_FUNCTIONAL_OBJECT_PROPERTY)) {

                } else if (axiom.is(IAxiom.INVERSE_OBJECT_PROPERTIES)) {

                } else if (axiom.is(IAxiom.IRREFLEXIVE_OBJECT_PROPERTY)) {

                } else if (axiom.is(IAxiom.NEGATIVE_DATA_PROPERTY_ASSERTION)) {

                } else if (axiom.is(IAxiom.NEGATIVE_OBJECT_PROPERTY_ASSERTION)) {

                } else if (axiom.is(IAxiom.REFLEXIVE_OBJECT_PROPERTY)) {

                } else if (axiom.is(IAxiom.SUB_ANNOTATION_PROPERTY_OF)) {

                } else if (axiom.is(IAxiom.SUB_DATA_PROPERTY)) {

                    OWLDataProperty subdprop = (OWLDataProperty) findProperty(axiom.getArgument(1)
                            .toString(), true, errors);
                    OWLDataProperty superdprop = (OWLDataProperty) findProperty(axiom.getArgument(0)
                            .toString(), true, errors);

                    if (subdprop != null && superdprop != null) {
                        OWL.INSTANCE.manager.addAxiom(this.ontology, factory
                                .getOWLSubDataPropertyOfAxiom(subdprop, superdprop));
                    }

                } else if (axiom.is(IAxiom.SUB_ANNOTATION_PROPERTY)) {

                    OWLAnnotationProperty suboprop = (OWLAnnotationProperty) findProperty(axiom.getArgument(1)
                            .toString(), false, errors);
                    OWLAnnotationProperty superoprop = (OWLAnnotationProperty) findProperty(axiom
                            .getArgument(0).toString(), false, errors);

                    if (suboprop != null && superoprop != null) {
                        OWL.INSTANCE.manager.addAxiom(this.ontology, factory
                                .getOWLSubAnnotationPropertyOfAxiom(suboprop, superoprop));
                    }

                } else if (axiom.is(IAxiom.SUB_OBJECT_PROPERTY)) {

                    OWLObjectProperty suboprop = (OWLObjectProperty) findProperty(axiom.getArgument(1)
                            .toString(), false, errors);
                    OWLObjectProperty superoprop = (OWLObjectProperty) findProperty(axiom.getArgument(0)
                            .toString(), false, errors);

                    if (suboprop != null && superoprop != null) {
                        OWL.INSTANCE.manager.addAxiom(this.ontology, factory
                                .getOWLSubObjectPropertyOfAxiom(suboprop, superoprop));
                    }

                } else if (axiom.is(IAxiom.SUB_PROPERTY_CHAIN_OF)) {

                } else if (axiom.is(IAxiom.SYMMETRIC_OBJECT_PROPERTY)) {

                } else if (axiom.is(IAxiom.TRANSITIVE_OBJECT_PROPERTY)) {

                } else if (axiom.is(IAxiom.SWRL_RULE)) {

                } else if (axiom.is(IAxiom.HAS_KEY)) {

                } else if (axiom.is(IAxiom.ANNOTATION_ASSERTION)) {

                    OWLAnnotationProperty property = findAnnotationProperty(axiom.getArgument(1)
                            .toString(), errors);
                    Object value = axiom.getArgument(2);
                    OWLLiteral literal = null;
                    OWLEntity target = findKnowledge(axiom.getArgument(0).toString(), errors);

                    if (target != null) {

                        if (value instanceof String)
                            literal = factory.getOWLLiteral(StringUtil.pack((String) value));
                        else if (value instanceof Integer)
                            literal = factory.getOWLLiteral((Integer) value);
                        else if (value instanceof Long)
                            literal = factory.getOWLLiteral((Long) value);
                        else if (value instanceof Float)
                            literal = factory.getOWLLiteral((Float) value);
                        else if (value instanceof Double)
                            literal = factory.getOWLLiteral((Double) value);
                        else if (value instanceof Boolean)
                            literal = factory.getOWLLiteral((Boolean) value);

                        /*
                         * TODO determine literal from type of value and property
                         */
                        if (property != null && literal != null) {
                            OWLAnnotation annotation = factory.getOWLAnnotation(property, literal);
                            OWL.INSTANCE.manager.addAxiom(this.ontology, factory
                                    .getOWLAnnotationAssertionAxiom(target.getIRI(), annotation));
                        }
                    }

                } else if (axiom.is(IAxiom.ANNOTATION_PROPERTY_DOMAIN)) {

                } else if (axiom.is(IAxiom.ANNOTATION_PROPERTY_RANGE)) {

                }

            } catch (OWLOntologyChangeException e) {
                errors.add(e.getMessage());
            }

        }

        scan();
        return errors;
    }

    /* must exist, can be property or class */
    private OWLEntity findKnowledge(String string, ArrayList<String> errors) {

        if (this.conceptIDs.containsKey(string)) {
            return findClass(string, errors);
        } else if (this.propertyIDs.contains(string)) {
            if (this.apropertyIDs.contains(string)) {
                return findAnnotationProperty(string, errors);
            }
            return findProperty(string, this.dpropertyIDs.contains(string), errors);
        }

        return null;
    }

    private OWLClass findClass(String c, ArrayList<String> errors) {

        OWLClass ret = null;

        if (c.equals("owl:Nothing")) {
            return ((Concept) OWL.INSTANCE.getNothing())._owl;
        } else if (c.equals("owl:Thing")) {
            return ((Concept) OWL.INSTANCE.getRootConcept())._owl;
        }

        if (c.contains(":")) {

            IConcept cc = OWL.INSTANCE.getConcept(c);
            if (cc == null) {
                errors.add("concept " + cc + " not found");
                return null;
            }

            /*
             * ensure ontology is imported
             */
            if (!cc.getNamespace().equals(this.id) && !this.imported.contains(cc.getNamespace())) {

                this.imported.add(cc.getNamespace());
                IRI importIRI = ((Ontology) cc.getOntology()).ontology.getOntologyID().getOntologyIRI();
                OWLImportsDeclaration importDeclaraton = this.ontology.getOWLOntologyManager()
                        .getOWLDataFactory().getOWLImportsDeclaration(importIRI);
                OWL.INSTANCE.manager.applyChange(new AddImport(this.ontology, importDeclaraton));
            }

            ret = ((Concept) cc)._owl;

        } else {

            IConcept cc = conceptIDs.get(c);

            if (cc != null) {
                ret = ((Concept) cc)._owl;
            } else {

                ret = this.ontology.getOWLOntologyManager().getOWLDataFactory()
                        .getOWLClass(IRI.create(this.prefix + "#" + c));

                this.conceptIDs.put(c, new Concept(ret, c, OWL.emptyType));
            }
        }

        return ret;
    }

    private OWLEntity findProperty(String c, boolean isData, ArrayList<String> errors) {

        OWLEntity ret = null;

        if (c.contains(":")) {

            IProperty cc = OWL.INSTANCE.getProperty(c);
            if (cc == null) {
                errors.add("property " + cc + " not found");
                return null;
            }

            /*
             * ensure ontology is imported
             */
            if (!cc.getNamespace().equals(this.id) && !this.imported.contains(cc.getNamespace())) {

                this.imported.add(cc.getNamespace());
                IRI importIRI = ((Ontology) cc.getOntology()).ontology.getOntologyID().getOntologyIRI();
                OWLImportsDeclaration importDeclaraton = this.ontology.getOWLOntologyManager()
                        .getOWLDataFactory().getOWLImportsDeclaration(importIRI);
                OWL.INSTANCE.manager.applyChange(new AddImport(this.ontology, importDeclaraton));
            }

            ret = ((Property) cc)._owl;

            if (isData && ret instanceof OWLObjectProperty) {
                throw new KlabValidationException(cc + " is an object property: data expected");
            }
            if (!isData && ret instanceof OWLDataProperty) {
                throw new KlabValidationException(cc + " is a data property: object expected");
            }

        } else {
            ret = isData
                    ? this.ontology.getOWLOntologyManager().getOWLDataFactory()
                            .getOWLDataProperty(IRI.create(this.prefix + "#" + c))
                    : this.ontology.getOWLOntologyManager().getOWLDataFactory()
                            .getOWLObjectProperty(IRI.create(this.prefix + "#" + c));

            if (isData) {
                this.dpropertyIDs.add(c);
            } else {
                this.opropertyIDs.add(c);
            }
            this.propertyIDs.add(c);
        }

        return ret;
    }

    private OWLAnnotationProperty findAnnotationProperty(String c, ArrayList<String> errors) {

        OWLAnnotationProperty ret = null;

        if (c.equals("rdfs:label")) {
            return this.ontology.getOWLOntologyManager().getOWLDataFactory().getRDFSLabel();
        } else if (c.equals("rdfs:comment")) {
            return this.ontology.getOWLOntologyManager().getOWLDataFactory().getRDFSComment();
        } else if (c.equals("rdfs:seealso")) {
            return this.ontology.getOWLOntologyManager().getOWLDataFactory().getRDFSSeeAlso();
        } else if (c.equals("rdfs:isdefinedby")) {
            return this.ontology.getOWLOntologyManager().getOWLDataFactory().getRDFSIsDefinedBy();
        }

        if (c.contains(":")) {

            IProperty cc = OWL.INSTANCE.getProperty(c);
            if (cc != null) {
                OWLEntity e = ((Property) cc)._owl;
                if (e instanceof OWLAnnotationProperty)
                    return (OWLAnnotationProperty) e;
            } else {
                SemanticType ct = new SemanticType(c);
                IOntology ontology = OWL.INSTANCE.getOntology(ct.getName());
                if (ontology == null) {
                    INamespace ns = Namespaces.INSTANCE.getNamespace(ct.getName());
                    if (ns != null)
                        ontology = ns.getOntology();
                }
                if (ontology != null) {
                    ret = ((Ontology) ontology).ontology.getOWLOntologyManager().getOWLDataFactory()
                            .getOWLAnnotationProperty(IRI
                                    .create(((Ontology) ontology).prefix + "#" + ct.getName()));
                }
            }
        } else {
            ret = this.ontology.getOWLOntologyManager().getOWLDataFactory()
                    .getOWLAnnotationProperty(IRI.create(this.prefix + "#" + c));
            this.apropertyIDs.add(c);
            this.propertyIDs.add(c);
        }

        return ret;
    }

    @Override
    public IMetadata getMetadata() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName() {
        return this.id;
    }

    @Override
    public int getConceptCount() {
        return this.conceptIDs.size();
    }

    @Override
    public int getPropertyCount() {
        return this.propertyIDs.size();
    }

    public void setResourceUrl(String string) {
        this.resourceUrl = string;
    }

    /**
     * Return the URL of the resource this was read from, or null if it was created by the API.
     * 
     * @return URL of source
     */
    public String getResourceUrl() {
        return this.resourceUrl;
    }

    public IConcept createConcept(String newName, EnumSet<Type> type) {

        IConcept ret = getConcept(newName);
        if (ret == null) {
            ArrayList<IAxiom> ax = new ArrayList<>();
            ax.add(Axiom.ClassAssertion(newName, type));
            define(ax);
            ret = getConcept(newName);
        }
        return ret;

    }

    public IProperty createProperty(String newName, boolean isData) {

        IProperty ret = getProperty(newName);
        if (ret == null) {
            ArrayList<IAxiom> ax = new ArrayList<>();
            ax.add(isData ? Axiom.DataPropertyAssertion(newName) : Axiom.ObjectPropertyAssertion(newName));
            define(ax);
            ret = getProperty(newName);
        }
        return ret;

    }

    public boolean isInternal() {
        return this.isInternal;
    }

    public void setInternal(boolean b) {
        this.isInternal = b;
    }

    public void createReasoner() {

        try {
            this.ontology.getOWLOntologyManager()
                    .loadOntology(IRI.create(this.ontology.getOntologyID().getOntologyIRI().toString()));
        } catch (OWLOntologyCreationException e) {
            throw new KlabInternalErrorException(e);
        }

    }

    public void addImport(IOntology ontology) {

        if (!this.ontology.getImports().contains(((Ontology) ontology).ontology)) {
            OWLDataFactory factory = this.ontology.getOWLOntologyManager().getOWLDataFactory();
            OWLImportsDeclaration imp = factory.getOWLImportsDeclaration(IRI
                    .create(((Ontology) ontology).ontology.getOntologyID().getOntologyIRI().toString()));
            this.ontology.getOWLOntologyManager().applyChange(new AddImport(this.ontology, imp));
            this.imported.add(ontology.getName());
        }
    }

    @Override
    public String toString() {
        return "<O " + id + " (" + ontology.getOntologyID().getOntologyIRI() + ")>";
    }

    public OWLOntology getOWLOntology() {
        return this.ontology;
    }

    /**
     * Get the unique ID for a concept with this definition, if any has been created.
     * 
     * @param definition
     * @return the ID or null
     */
    public String getIdForDefinition(String definition) {
        return this.definitionIds.get(definition);
    }

    /**
     * Create a new ID for this definition and store it.
     * 
     * @param definition
     * @return the new ID
     */
    public String createIdForDefinition(String definition) {
        String id = getName().replaceAll("\\.", "_").toUpperCase() + "_"
                + String.format("%09d", idCounter.incrementAndGet());
        this.definitionIds.put(definition, id);
        return id;
    }

    @Override
    public Set<IOntology> getImports(boolean recursive) {
        return getImports(this, recursive, new HashSet<>());
    }

    private Set<IOntology> getImports(Ontology ontology, boolean recursive, Set<IOntology> ret) {
        for (String i : ontology.imported) {
            IOntology o = OWL.INSTANCE.getOntology(i);
            if (o != null) {
                boolean isnew = ret.add(o);
                if (recursive && isnew) {
                    getImports((Ontology) o, recursive, ret);
                }
            }
        }
        return ret;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ontology == null) ? 0 : ontology.getOntologyID().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Ontology other = (Ontology) obj;
        if (ontology == null) {
            if (other.ontology != null)
                return false;
        } else if (!ontology.getOntologyID().equals(other.ontology.getOntologyID()))
            return false;
        return true;
    }

	@Override
	public IConcept getIdentity(String authority, String authorityIdentity) {
		IAuthority.Identity identity = Authorities.INSTANCE.getIdentity(authority, authorityIdentity);
		return null;
	}

    // public IIndividual getSingletonIndividual(IConcept concept) {
    // String iName = "the" + concept.getLocalName();
    // if (individuals.containsKey(iName)) {
    // return individuals.get(iName);
    // }
    // IIndividual ret = new Individual();
    // ((Individual) ret).define(concept, iName, this);
    // return ret;
    // }
    //
    // public void linkIndividuals(IIndividual source, IIndividual destination, IProperty link) {
    //
    // OWLObjectPropertyAssertionAxiom axiom = manager.manager.getOWLDataFactory()
    // .getOWLObjectPropertyAssertionAxiom(((Property) link)._owl
    // .asOWLObjectProperty(), ((Individual) source).individual, ((Individual)
    // destination).individual);
    //
    // AddAxiom addAxiom = new AddAxiom(ontology, axiom);
    // manager.manager.applyChange(addAxiom);
    // }
}
