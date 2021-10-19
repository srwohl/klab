package org.integratedmodelling.klab.api.knowledge;

import java.util.Collection;

import org.integratedmodelling.klab.api.provenance.IArtifact;

/**
 * The non-semantic version of an authority; used to match statistical codelists that haven't yet
 * made into classifications worth of being an officially endorsed authority, through a dedicated
 * authority builder. Codelists can be included in and referenced from resources. A codelist may be
 * promoted to an authority.
 * <p>
 * t In a codelist, there may be no values associated with keys (the string codes are all it
 * provides) or the modeler may associate values of a k.LAB-compatible artifact type to the keys. In
 * that case, the mapping admits multiple keys to point to the same value but not the other way
 * around: when looking up the key for a value, the answer must be crisp, so a "preferential" key
 * must be identified in a two-way table.
 * 
 * @author Ferd
 *
 */
public interface ICodelist {

    /**
     * Name of codelist. Usually a string with agency/name/version.
     * 
     * @return
     */
    String getName();

    /**
     * 
     * @return
     */
    String getDescription();

    /**
     * If not null, specifies the authority this either incarnates (if {@link #isAuthority()}) or
     * maps to. If this is not null, {@link #getType()} must return CONCEPT.
     * 
     * @return
     */
    String getAuthorityId();

    /**
     * If true, the codelist is exposed as an authority, referenceable through the URN of the
     * resource containing it. There may still be an authority that this maps to without being an
     * authority itself.
     * 
     * @return
     */
    boolean isAuthority();

    /**
     * If the codelist depends on a worldview, return true. Worldview-dependent codelists should be
     * as few as possible and ideally should not exist.
     * 
     * @return
     */
    String getWorldview();

    /**
     * The type corresponding to the mapped value. Must agree with T.
     * 
     * @return
     */
    IArtifact.Type getType();

    /**
     * A regex or other interpretive key needed to turn the codes into the artifact type, without
     * remapping each single one of them. Those which do have a mapping will use the mapping instead.
     * 
     * @return
     */
    String getPattern();

    /**
     * Value corresponding to a key. Multiple values aren't possible but different keys may point to
     * the same value.
     * 
     * @param key
     * @return
     */
    Object value(String key);

    /**
     * All keys correspondent to a value.
     * 
     * @param value
     * @return
     */
    Collection<String> keys(Object value);

    /**
     * The preferential key correspondent to a value.
     * 
     * @param value
     * @return
     */
    String key(Object value);

}
