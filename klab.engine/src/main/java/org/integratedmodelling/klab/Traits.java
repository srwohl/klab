package org.integratedmodelling.klab;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.integratedmodelling.kim.api.IKimConcept.Type;
import org.integratedmodelling.klab.api.knowledge.IAxiom;
import org.integratedmodelling.klab.api.knowledge.IConcept;
import org.integratedmodelling.klab.api.knowledge.IOntology;
import org.integratedmodelling.klab.api.knowledge.IProperty;
import org.integratedmodelling.klab.api.services.ITraitService;
import org.integratedmodelling.klab.common.LogicalConnector;
import org.integratedmodelling.klab.engine.resources.CoreOntology.NS;
import org.integratedmodelling.klab.exceptions.KlabValidationException;
import org.integratedmodelling.klab.owl.Axiom;
import org.integratedmodelling.klab.owl.Concept;
import org.integratedmodelling.klab.owl.OWL;
import org.integratedmodelling.klab.owl.ObservableBuilder;
import org.integratedmodelling.klab.owl.Ontology;
import org.integratedmodelling.klab.utils.Pair;

public enum Traits implements ITraitService {
    
	INSTANCE;

	private Traits() {
		Services.INSTANCE.registerService(this, ITraitService.class);
	}
	
    @Override
    public Collection<IConcept> getTraits(IConcept concept) {
        Set<IConcept> ret = new HashSet<>();
        ret.addAll(OWL.INSTANCE.getRestrictedClasses((IConcept) concept, Concepts.p(NS.HAS_REALM_PROPERTY)));
        ret.addAll(OWL.INSTANCE
                .getRestrictedClasses((IConcept) concept, Concepts.p(NS.HAS_IDENTITY_PROPERTY)));
        ret.addAll(OWL.INSTANCE
                .getRestrictedClasses((IConcept) concept, Concepts.p(NS.HAS_ATTRIBUTE_PROPERTY)));
        return ret;
    }

    @Override
    public Collection<IConcept> getDirectTraits(IConcept concept) {
        Set<IConcept> ret = new HashSet<>();
        ret.addAll(OWL.INSTANCE
                .getDirectRestrictedClasses((IConcept) concept, Concepts.p(NS.HAS_REALM_PROPERTY)));
        ret.addAll(OWL.INSTANCE
                .getDirectRestrictedClasses((IConcept) concept, Concepts.p(NS.HAS_IDENTITY_PROPERTY)));
        ret.addAll(OWL.INSTANCE
                .getDirectRestrictedClasses((IConcept) concept, Concepts.p(NS.HAS_ATTRIBUTE_PROPERTY)));
        return ret;
    }

    @Override
    public Collection<IConcept> getIdentities(IConcept concept) {
        return OWL.INSTANCE.getRestrictedClasses((IConcept) concept, Concepts.p(NS.HAS_IDENTITY_PROPERTY));
    }

    @Override
    public IConcept getNegated(IConcept concept) {
        Collection<IConcept> ret = OWL.INSTANCE
                .getRestrictedClasses((IConcept) concept, Concepts.p(NS.IS_NEGATION_OF));
        return ret.size() > 0 ? ret.iterator().next() : null;
    }

    @Override
    public Collection<IConcept> getAttributes(IConcept concept) {
        return OWL.INSTANCE.getRestrictedClasses((IConcept) concept, Concepts.p(NS.HAS_ATTRIBUTE_PROPERTY));
    }

    @Override
    public Collection<IConcept> getRealms(IConcept concept) {
        return OWL.INSTANCE.getRestrictedClasses((IConcept) concept, Concepts.p(NS.HAS_REALM_PROPERTY));
    }

    @Override
    public IConcept getBaseParentTrait(IConcept trait) {

        String orig = trait.getMetadata().get(NS.ORIGINAL_TRAIT, String.class);
        if (orig != null) {
            trait = Concepts.c(orig);
        }

        /*
         * there should only be one of these or none.
         */
        if (trait.getMetadata().get(NS.BASE_DECLARATION) != null) {
            return trait;
        }

        for (IConcept c : trait.getParents()) {
            IConcept r = getBaseParentTrait(c);
            if (r != null) {
                return r;
            }
        }
        return null;
    }

    @Override
    public boolean hasTrait(IConcept type, IConcept trait) {

        for (IConcept c : getTraits(type)) {
            if (c.is(trait)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean hasParentTrait(IConcept type, IConcept trait) {

        for (IConcept c : getTraits(type)) {
            if (trait.is(c)) {
                return true;
            }
        }

        return false;
    }

    public void restrict(IConcept target, IProperty property, LogicalConnector how, Collection<IConcept> fillers, IOntology ontology)
            throws KlabValidationException {

        /*
         * divide up in bins according to base trait; take property from annotation;
         * restrict each group.
         */
        Map<IConcept, List<IConcept>> pairs = new HashMap<>();
        for (IConcept t : fillers) {
            IConcept base = getBaseParentTrait(t);
            if (!pairs.containsKey(base)) {
                pairs.put(base, new ArrayList<>());
            }
            pairs.get(base).add(t);
        }

        for (IConcept base : pairs.keySet()) {

            String prop = base.getMetadata().get(NS.TRAIT_RESTRICTING_PROPERTY, String.class);
            if (prop == null || Concepts.INSTANCE.getProperty(prop) == null) {
                if (base.is(Type.SUBJECTIVE)) {
                    /*
                     * we can assign any subjective traits to anything
                     */
                    prop = NS.HAS_SUBJECTIVE_TRAIT_PROPERTY;
                } else {
                    throw new KlabValidationException("cannot find property to restrict for trait " + base);
                }
            }
            // System.out.println("TRAIT " + pairs.get(base) + " for " + target + " with " +
            // prop);
            OWL.INSTANCE.restrictSome(target, Concepts.p(prop), how, pairs.get(base), (Ontology) ontology);
        }
    }

    /**
     * Analyze an observable concept and return the main observable with all the
     * original identities and realms but no attributes; separately, return the list
     * of the attributes that were removed.
     * 
     * @param observable
     * @return attribute profile
     * @throws KlabValidationException
     */
    public Pair<IConcept, Collection<IConcept>> separateAttributes(IConcept observable)
            throws KlabValidationException {

        IConcept obs = Observables.INSTANCE.getCoreObservable(observable);
        ArrayList<IConcept> tret = new ArrayList<>();
        ArrayList<IConcept> keep = new ArrayList<>();

        for (IConcept zt : getTraits(observable)) {
            if (zt.is(Concepts.c(NS.CORE_IDENTITY)) || zt.is(Concepts.c(NS.CORE_REALM))) {
                keep.add(zt);
            } else {
                tret.add(zt);
            }
        }

        IConcept root = null; // Observables.declareObservable((IConcept) (obs == null ? observable
        // : obs), keep, Observables.getContextType(observable), Observables
        // .getInherentType(observable));

        return new Pair<>(root, tret);
    }

    public void addTrait(Concept main, IConcept trait, IOntology ontology) throws KlabValidationException {
        IProperty property = null;
        if (trait.is(Type.IDENTITY)) {
            property = Concepts.p(NS.HAS_IDENTITY_PROPERTY);
        } else if (trait.is(Type.REALM)) {
            property = Concepts.p(NS.HAS_REALM_PROPERTY);
        } else if (trait.is(Type.ATTRIBUTE)) {
            property = Concepts.p(NS.HAS_ATTRIBUTE_PROPERTY);
        }
        if (property != null) {
            restrict(main, property, LogicalConnector.UNION, Collections.singleton(trait), (Ontology)ontology);
        }
    }

    public IConcept makeNegation(Concept attribute, IOntology ontology) {

        IConcept negation = getNegated(attribute);
        if (negation != null) {
            return negation;
        }

        String prop = attribute.getMetadata().get(NS.TRAIT_RESTRICTING_PROPERTY, String.class);
        String conceptId = "Not" + ObservableBuilder.getCleanId(attribute);
        Concept ret = attribute.getOntology().getConcept(conceptId);
        IConcept parent = attribute.getParent();

        if (ret == null) {

            EnumSet<Type> newType = EnumSet.copyOf(attribute.getTypeSet());

            ArrayList<IAxiom> ax = new ArrayList<>();
            ax.add(Axiom.ClassAssertion(conceptId, newType));
            ax.add(Axiom.SubClass(parent.toString(), conceptId));
            ax.add(Axiom.AnnotationAssertion(conceptId, NS.BASE_DECLARATION, "true"));
            ax.add(Axiom.AnnotationAssertion(conceptId, NS.REFERENCE_NAME_PROPERTY, "not_" + attribute.getReferenceName()));
            ax.add(Axiom.AnnotationAssertion(conceptId, "rdfs:label", "Not"
                    + ObservableBuilder.getCleanId(attribute)));
            ax.add(Axiom.AnnotationAssertion(conceptId, NS.CONCEPT_DEFINITION_PROPERTY, "not  "
                    + attribute.getDefinition()));

            if (prop != null) {
                ax.add(Axiom.AnnotationAssertion(conceptId, NS.TRAIT_RESTRICTING_PROPERTY, prop));
            }

            attribute.getOntology().define(ax);

            ret = attribute.getOntology().getConcept(conceptId);

            OWL.INSTANCE.restrictSome(ret, Concepts.p(NS.IS_NEGATION_OF), attribute, (Ontology)ontology);
        }

        return ret;
    }

}
