/*******************************************************************************
 *  Copyright (C) 2007, 2015:
 *  
 *    - Ferdinando Villa <ferdinando.villa@bc3research.org>
 *    - integratedmodelling.org
 *    - any other authors listed in @author annotations
 *
 *    All rights reserved. This file is part of the k.LAB software suite,
 *    meant to enable modular, collaborative, integrated 
 *    development of interoperable data and model components. For
 *    details, see http://integratedmodelling.org.
 *    
 *    This program is free software; you can redistribute it and/or
 *    modify it under the terms of the Affero General Public License 
 *    Version 3 or any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but without any warranty; without even the implied warranty of
 *    merchantability or fitness for a particular purpose.  See the
 *    Affero General Public License for more details.
 *  
 *     You should have received a copy of the Affero General Public License
 *     along with this program; if not, write to the Free Software
 *     Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *     The license is also available at: https://www.gnu.org/licenses/agpl.html
 *******************************************************************************/
package org.integratedmodelling.klab.owl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.integratedmodelling.klab.api.knowledge.IConcept;
import org.integratedmodelling.klab.api.knowledge.IKnowledge;
import org.integratedmodelling.klab.api.knowledge.IMetadata;
import org.integratedmodelling.klab.api.knowledge.IOntology;
import org.integratedmodelling.klab.api.knowledge.IProperty;
import org.integratedmodelling.klab.api.knowledge.ISemantic;
import org.integratedmodelling.klab.exceptions.KlabValidationException;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;

public class Property extends Knowledge implements IProperty {

    String    _id;
    String    _cs;
    OWLEntity _owl;

    public Property(OWLEntity p, String cs) {
        _owl = p;
        _id = _owl.getIRI().getFragment();
        _cs = cs;
    }

    @Override
    public String getNamespace() {
        return _cs;
    }

    @Override
    public String getName() {
        return _id;
    }

    @Override
    public boolean is(ISemantic s) {
        /*
         * TODO use reasoner as appropriate
         */
        IKnowledge p = s instanceof Property ? (IProperty)s : s.getType();
        return p instanceof Property && (p.equals(this) || getAllParents().contains(p));
    }

    @Override
    public String getURI() {
        return _owl.getIRI().toString();
    }

    @Override
    public IOntology getOntology() {
        return OWL.INSTANCE.getOntology(getNamespace());
    }

    @Override
    public boolean isClassification() {
        // FIXME hmmm. Do we still need this?
        return false;
    }

    @Override
    public boolean isLiteralProperty() {
        return _owl.isOWLDataProperty() || _owl.isOWLAnnotationProperty();
    }

    @Override
    public boolean isObjectProperty() {
        return _owl.isOWLObjectProperty();
    }

    @Override
    public boolean isAnnotation() {
        return _owl.isOWLAnnotationProperty();
    }

    public OWLEntity getOWLEntity() {
        return _owl;
    }
    
    @Override
    public IProperty getInverseProperty() {
        Property ret = null;

        synchronized (_owl) {
            if (_owl.isOWLObjectProperty()) {

                Set<OWLObjectPropertyExpression> dio = _owl.asOWLObjectProperty().getInverses(ontology());

                if (dio.size() > 1)
                    throw new KlabValidationException("taking the inverse of property " + this
                            + ", which has multiple inverses");

                if (dio.size() > 0) {
                    OWLObjectProperty op = dio.iterator().next().asOWLObjectProperty();
                    ret = new Property(op, OWL.INSTANCE.getConceptSpace(op.getIRI()));
                }
            }
        }
        return ret;
    }

    @Override
    public Collection<IConcept> getRange() {
        Set<IConcept> ret = new HashSet<IConcept>();
        synchronized (_owl) {
            if (_owl.isOWLDataProperty()) {

                for (OWLDataRange c : _owl.asOWLDataProperty().getRanges(OWL.INSTANCE.manager.getOntologies())) {

                    if (c.isDatatype()) {
                        OWLDatatype type = c.asOWLDatatype();
                        IConcept tltype = OWL.INSTANCE.getDatatypeMapping(type.getIRI().toString());
                        if (tltype != null) {
                            ret.add(tltype);
                        }
                    }
                }
            } else if (_owl.isOWLObjectProperty()) {
                for (OWLClassExpression c : _owl.asOWLObjectProperty().getRanges(
                        OWL.INSTANCE.manager.getOntologies())) {
                    if (!c.isAnonymous())
                        ret.add(OWL.INSTANCE.getExistingOrCreate(c.asOWLClass()));
                }
            }
        }
        return ret;
    }

    @Override
    public Collection<IConcept> getPropertyDomain() {

        Set<IConcept> ret = new HashSet<IConcept>();
        synchronized (this._owl) {
            if (_owl.isOWLDataProperty()) {
                for (OWLClassExpression c : _owl.asOWLDataProperty().getDomains(
                        OWL.INSTANCE.manager.getOntologies())) {
                    ret.add(OWL.INSTANCE.getExistingOrCreate(c.asOWLClass()));
                }
            } else if (_owl.isOWLObjectProperty()) {
                for (OWLClassExpression c : _owl.asOWLObjectProperty().getDomains(
                        OWL.INSTANCE.manager.getOntologies())) {
                    ret.add(OWL.INSTANCE.getExistingOrCreate(c.asOWLClass()));
                }
            }
        }
        return ret;
    }

    @Override
    public boolean isAbstract() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public IProperty getParent() {

        Collection<IProperty> pars = getParents();

        if (pars.size() > 1)
            throw new KlabValidationException("asking for single parent of multiple-inherited property "
                    + this);

        return pars.size() == 0 ? null : pars.iterator().next();
    }

    @Override
    public Collection<IProperty> getParents() {

        Set<IProperty> ret = new HashSet<IProperty>();
        Set<OWLOntology> onts = OWL.INSTANCE.manager.getOntologies();

        /*
         * TODO use reasoner as appropriate
         */
        synchronized (_owl) {
            if (_owl.isOWLDataProperty()) {
                for (OWLOntology o : onts) {
                    for (OWLDataPropertyExpression p : _owl.asOWLDataProperty().getSuperProperties(o)) {
                        ret.add(new Property(p.asOWLDataProperty(), OWL.INSTANCE.getConceptSpace(p
                                .asOWLDataProperty().getIRI())));
                    }
                }
            } else if (_owl.isOWLObjectProperty()) {
                for (OWLOntology o : onts) {
                    for (OWLObjectPropertyExpression p : _owl.asOWLObjectProperty().getSuperProperties(o)) {
                        ret.add(new Property(p.asOWLObjectProperty(), OWL.INSTANCE.getConceptSpace(p
                                .asOWLObjectProperty().getIRI())));
                    }
                }
            }
        }

        return ret;
    }

    @Override
    public Collection<IProperty> getAllParents() {
        Set<IProperty> ret = new HashSet<IProperty>();

        synchronized (_owl) {
//            if (_manager.reasoner != null) {
//
//                // try {
//                // if (_owl.isOWLObjectProperty()) {
//                // Set<Set<OWLObjectProperty>> parents =
//                // KR().getPropertyReasoner()
//                // .getAncestorProperties(entity
//                // .asOWLObjectProperty());
//                // Set<OWLObjectProperty> subClses = OWLReasonerAdapter
//                // .flattenSetOfSets(parents);
//                // for (OWLObjectProperty cls : subClses) {
//                // ret.add(new Property(cls));
//                // }
//                // } else if (entity.isOWLDataProperty()) {
//                // Set<Set<OWLDataProperty>> parents =
//                // KR().getPropertyReasoner()
//                // .getAncestorProperties(entity
//                // .asOWLDataProperty());
//                // Set<OWLDataProperty> subClses = OWLReasonerAdapter
//                // .flattenSetOfSets(parents);
//                // for (OWLDataProperty cls : subClses) {
//                // ret.add(new Property(cls));
//                // }
//                // }
//                // return ret;
//                //
//                // } catch (OWLException e) {
//                // // just continue to dumb method
//                // }
//
//            } else {

                for (IProperty c : getParents()) {
                    ret.add(c);
                    ret.addAll(c.getAllParents());
                }
//            }
        }
        return ret;
    }

    @Override
    public Collection<IProperty> getChildren() {

        Set<IProperty> ret = new HashSet<IProperty>();

        Set<OWLOntology> onts = OWL.INSTANCE.manager.getOntologies();

        if (_owl.isOWLDataProperty()) {
            for (OWLOntology o : onts) {
                synchronized (this._owl) {
                    for (OWLDataPropertyExpression p : _owl.asOWLDataProperty().getSubProperties(o)) {
                        ret.add(new Property(p.asOWLDataProperty(), OWL.INSTANCE.getConceptSpace(p
                                .asOWLDataProperty().getIRI())));
                    }
                }
            }
        } else if (_owl.isOWLObjectProperty()) {
            for (OWLOntology o : onts) {
                synchronized (this._owl) {
                    for (OWLObjectPropertyExpression p : _owl.asOWLObjectProperty().getSubProperties(o)) {
                        ret.add(new Property(p.asOWLObjectProperty(), OWL.INSTANCE.getConceptSpace(p
                                .asOWLObjectProperty().getIRI())));
                    }
                }
            }
        }

        return ret;
    }

    @Override
    public Collection<IProperty> getAllChildren() {

        Set<IProperty> ret = new HashSet<IProperty>();
        for (IProperty c : getChildren()) {

            ret.add(c);
            for (IProperty p : c.getChildren()) {
                ret.addAll(p.getAllChildren());
            }
        }

        return ret;
    }

    @Override
    public boolean isFunctional() {
        return _owl.isOWLDataProperty() ? _owl.asOWLDataProperty().isFunctional(ontology()) : _owl
                .asOWLObjectProperty().isFunctional(ontology());
    }

    @Override
    public IMetadata getMetadata() {
        return new OWLMetadata(_owl, ((Ontology) getOntology()).ontology);
    }

    private OWLOntology ontology() {
        return ((Ontology) getOntology()).ontology;
    }

    @Override
    public String toString() {
        return getNamespace() + ":" + _id;
    }

    @Override
    public synchronized Set<IConcept> getSemanticClosure() {

//        if (_manager.reasoner != null) {
//
//            // TODO for now it will just use the asserted children.
//            // HashSet<IKnowledge> ret = new HashSet<>();
//            // NodeSet<OWLClass> cset = _manager.reasoner.getSub(_owl,
//            // false);
//            // for (OWLClass cl : cset.getFlattened()) {
//            // ret.add(new Concept(cl, _manager, _manager.getConceptSpace(cl
//            // .getIRI())));
//            // }
//            // return ret;
//        }

        // TODO use backing concept for relationships.
        Set<IConcept> ret = collectChildren(new HashSet<IConcept>());
//        ret.add(this);

        return ret;
    }

    private Set<IConcept> collectChildren(Set<IConcept> hashSet) {

        for (IProperty c : getChildren()) {
//            if (!hashSet.contains(c))
//                ((Property) c).collectChildren(hashSet);
//            hashSet.add(c);
        }
        return hashSet;
    }

    @Override
    public IConcept getType() {
        if (_id.startsWith("p")) {
            return getOntology().getConcept(this._id.substring(1));
        }
        return null;
    }

    @Override
    public String getUrn() {
        return getNamespace() + ":" + _id;
    }

    @Override
    public String getReferenceName() {
        return getName();
    }

}
