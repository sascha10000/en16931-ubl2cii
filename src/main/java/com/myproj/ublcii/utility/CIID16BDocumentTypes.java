/**
 * 
 */
package com.myproj.ublcii.utility;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.xml.validation.Schema;

import com.helger.commons.annotation.PresentForCodeCoverage;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.collection.impl.CommonsHashMap;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.collection.impl.ICommonsSet;

/**
 * CII document type map. Provides sanity methods for accessing CII D16B
 * document types based on different information items.
 *
 * @author Vartika Gupta
 */
@Immutable
public final class CIID16BDocumentTypes {
	/** Maps namespaces to document types */
	private static final ICommonsMap<String, ECIID16BDocumentType> s_aNamespace2DocType = new CommonsHashMap<>();

	/** Maps local names to document types */
	private static final ICommonsMap<String, ECIID16BDocumentType> s_aLocalName2DocType = new CommonsHashMap<>();

	static {
		// Register all CII D16B document types
		for (final ECIID16BDocumentType eDocType : ECIID16BDocumentType.values()) {
			// add to namespace map
			final String sNamespace = eDocType.getNamespaceURI();
			if (s_aNamespace2DocType.containsKey(sNamespace))
				throw new IllegalArgumentException("The namespace '" + sNamespace + "' is already mapped!");
			s_aNamespace2DocType.put(sNamespace, eDocType);

			// add to local name map
			final String sLocalName = eDocType.getLocalName();
			if (s_aLocalName2DocType.containsKey(sLocalName))
				throw new IllegalArgumentException("The local name '" + sLocalName + "' is already mapped!");
			s_aLocalName2DocType.put(sLocalName, eDocType);
		}
	}

	@PresentForCodeCoverage
	private static final CIID16BDocumentTypes s_aInstance = new CIID16BDocumentTypes();

	private CIID16BDocumentTypes() {
	}

	/**
	 * @return A non-<code>null</code> set of all supported CII D16B namespaces.
	 */
	@Nonnull
	@ReturnsMutableCopy
	public static ICommonsSet<String> getAllNamespaces() {
		return s_aNamespace2DocType.copyOfKeySet();
	}

	/**
	 * Get the document type matching the passed namespace.
	 *
	 * @param sNamespace The namespace URI of any CII D16B document type. May be
	 *                   <code>null</code>.
	 * @return <code>null</code> if no CII D16B document type matching the specified
	 *         namespace URI exists.
	 */
	@Nullable
	public static ECIID16BDocumentType getDocumentTypeOfNamespace(@Nullable final String sNamespace) {
		return s_aNamespace2DocType.get(sNamespace);
	}

	/**
	 * Get the domain object class of the passed namespace.
	 *
	 * @param sNamespace The namespace URI of any CII D16B document type. May be
	 *                   <code>null</code>.
	 * @return <code>null</code> if no such CII D16B document type exists.
	 */
	@Nullable
	public static Class<?> getImplementationClassOfNamespace(@Nullable final String sNamespace) {
		final ECIID16BDocumentType eDocType = getDocumentTypeOfNamespace(sNamespace);
		return eDocType == null ? null : eDocType.getImplementationClass();
	}

	/**
	 * Get the XSD Schema object for the CII D16B document type of the passed
	 * namespace.
	 *
	 * @param sNamespace The namespace URI of any CII D16B document type. May be
	 *                   <code>null</code>.
	 * @return <code>null</code> if no such CII D16B document type exists.
	 */
	@Nullable
	public static Schema getSchemaOfNamespace(@Nullable final String sNamespace) {
		final ECIID16BDocumentType eDocType = getDocumentTypeOfNamespace(sNamespace);
		return eDocType == null ? null : eDocType.getSchema();
	}

	/**
	 * @return A non-<code>null</code> set of all supported CII D16B document
	 *         element local names.
	 */
	@Nonnull
	@ReturnsMutableCopy
	public static ICommonsSet<String> getAllLocalNames() {
		return s_aLocalName2DocType.copyOfKeySet();
	}

	/**
	 * Get the document type matching the passed document element local name.
	 *
	 * @param sLocalName The document element local name of any CII D16B document
	 *                   type. May be <code>null</code>.
	 * @return <code>null</code> if no CII D16B document type matching the specified
	 *         local name exists.
	 */
	@Nullable
	public static ECIID16BDocumentType getDocumentTypeOfLocalName(@Nullable final String sLocalName) {
		return s_aLocalName2DocType.get(sLocalName);
	}

	/**
	 * Get the domain object class of the passed document element local name.
	 *
	 * @param sLocalName The document element local name of any CII D16B document
	 *                   type. May be <code>null</code>.
	 * @return <code>null</code> if no such implementation class exists.
	 */
	@Nullable
	public static Class<?> getImplementationClassOfLocalName(@Nullable final String sLocalName) {
		final ECIID16BDocumentType eDocType = getDocumentTypeOfLocalName(sLocalName);
		return eDocType == null ? null : eDocType.getImplementationClass();
	}

	/**
	 * Get the CII D16B document type matching the passed implementation class.
	 *
	 * @param aImplClass The implementation class to use. May be <code>null</code>.
	 * @return <code>null</code> if the implementation class is <code>null</code> or
	 *         if no CII D16B document type has the specified implementation class.
	 */
	@Nullable
	public static ECIID16BDocumentType getDocumentTypeOfImplementationClass(@Nullable final Class<?> aImplClass) {
		if (aImplClass == null)
			return null;
		return ArrayHelper.findFirst(ECIID16BDocumentType.values(),
				eDocType -> eDocType.getImplementationClass().equals(aImplClass));
	}

	/**
	 * Get the XSD Schema object for the CII D16B document type of the passed
	 * document element local name.
	 *
	 * @param sLocalName The document element local name of any CII D16B document
	 *                   type. May be <code>null</code>.
	 * @return <code>null</code> if no such CII D16B document type exists.
	 */
	@Nullable
	public static Schema getSchemaOfLocalName(@Nullable final String sLocalName) {
		final ECIID16BDocumentType eDocType = getDocumentTypeOfLocalName(sLocalName);
		return eDocType == null ? null : eDocType.getSchema();
	}

	/**
	 * Get the XSD Schema object for the CII D16B document type of the passed
	 * implementation class.
	 *
	 * @param aImplClass The implementation class of any CII D16B document type. May
	 *                   be <code>null</code>.
	 * @return <code>null</code> if no such CII D16B document type exists.
	 */
	@Nullable
	public static Schema getSchemaOfImplementationClass(@Nullable final Class<?> aImplClass) {
		final ECIID16BDocumentType eDocType = getDocumentTypeOfImplementationClass(aImplClass);
		return eDocType == null ? null : eDocType.getSchema();
	}
}
