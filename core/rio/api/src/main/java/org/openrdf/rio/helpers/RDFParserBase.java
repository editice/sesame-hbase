/*
 * Copyright Aduna (http://www.aduna-software.com/) (c) 1997-2007.
 *
 * Licensed under the Aduna BSD-style license.
 */
package org.openrdf.rio.helpers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import info.aduna.net.ParsedURI;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.datatypes.XMLDatatypeUtil;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.model.vocabulary.XMLSchema;

import org.openrdf.rio.ParseErrorListener;
import org.openrdf.rio.ParseLocationListener;
import org.openrdf.rio.ParserConfig;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;

/**
 * Base class for {@link RDFParser}s offering common functionality for RDF
 * parsers.
 * 
 * @author Arjohn Kampman
 */
public abstract class RDFParserBase implements RDFParser {

	/**
	 * Vocabulary Prefixes of W3C Documents (Recommendations or Notes)
	 * 
	 * @see http://www.w3.org/2011/rdfa-context/rdfa-1.1
	 */
	private static final Map<String, String> defaultPrefix;
	static {
		Map<String, String> map = new HashMap<String, String>();
		map.put("grddl", "http://www.w3.org/2003/g/data-view#");
		map.put("ma", "http://www.w3.org/ns/ma-ont#");
		map.put("owl", "http://www.w3.org/2002/07/owl#");
		map.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		map.put("rdfa", "http://www.w3.org/ns/rdfa#");
		map.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		map.put("rif", "http://www.w3.org/2007/rif#");
		map.put("skos", "http://www.w3.org/2004/02/skos/core#");
		map.put("skosxl", "http://www.w3.org/2008/05/skos-xl#");
		map.put("wdr", "http://www.w3.org/2007/05/powder#");
		map.put("void", "http://rdfs.org/ns/void#");
		map.put("wdrs", "http://www.w3.org/2007/05/powder-s#");
		map.put("xhv", "http://www.w3.org/1999/xhtml/vocab#");
		map.put("xml", "http://www.w3.org/XML/1998/namespace");
		map.put("xsd", "http://www.w3.org/2001/XMLSchema#");
		defaultPrefix = Collections
				.unmodifiableMap(new HashMap<String, String>(map));
	}

	/*-----------*
	 * Variables *
	 *-----------*/

	/**
	 * The RDFHandler that will handle the parsed RDF.
	 */
	protected RDFHandler rdfHandler;

	/**
	 * An optional ParseErrorListener to report parse errors to.
	 */
	private ParseErrorListener errListener;

	/**
	 * An optional ParseLocationListener to report parse progress in the form of
	 * line- and column numbers to.
	 */
	private ParseLocationListener locationListener;

	/**
	 * The ValueFactory to use for creating RDF model objects.
	 */
	private ValueFactory valueFactory;

	/**
	 * Flag indicating whether the parser should verify the data it parses.
	 */
	private boolean verifyData;

	/**
	 * Flag indicating whether the parser should preserve bnode identifiers from
	 * the parsed data in the created BNode objects.
	 */
	private boolean preserveBNodeIDs;

	/**
	 * Flag indicating whether the parser should immediately stop parsing when
	 * it finds an error in the data.
	 */
	private boolean stopAtFirstError;

	/**
	 * Indicates how datatyped literals should be handled.
	 */
	private DatatypeHandling datatypeHandling;

	/**
	 * The base URI for resolving relative URIs.
	 */
	private ParsedURI baseURI;

	/**
	 * Mapping from blank node identifiers as used in the RDF document to the
	 * object created for it by the ValueFactory. This mapping is used to return
	 * identical BNode objects for recurring blank node identifiers.
	 */
	private Map<String, BNode> bNodeIDMap;

	/**
	 * Mapping from namespace prefixes to namespace names.
	 */
	private Map<String, String> namespaceTable;

	/*--------------*
	 * Constructors *
	 *--------------*/

	/**
	 * Creates a new RDFParserBase that will use a {@link ValueFactoryImpl} to
	 * create RDF model objects.
	 */
	public RDFParserBase() {
		this(new ValueFactoryImpl());
	}

	/**
	 * Creates a new RDFParserBase that will use the supplied ValueFactory to
	 * create RDF model objects.
	 * 
	 * @param valueFactory
	 *            A ValueFactory.
	 */
	public RDFParserBase(ValueFactory valueFactory) {
		bNodeIDMap = new HashMap<String, BNode>(16);
		namespaceTable = new HashMap<String, String>(16);

		setValueFactory(valueFactory);
		setParserConfig(new ParserConfig());
	}

	/*---------*
	 * Methods *
	 *---------*/

	public void setValueFactory(ValueFactory valueFactory) {
		this.valueFactory = valueFactory;
	}

	public void setRDFHandler(RDFHandler handler) {
		rdfHandler = handler;
	}

	public RDFHandler getRDFHandler() {
		return rdfHandler;
	}

	public void setParseErrorListener(ParseErrorListener el) {
		errListener = el;
	}

	public ParseErrorListener getParseErrorListener() {
		return errListener;
	}

	public void setParseLocationListener(ParseLocationListener el) {
		locationListener = el;
	}

	public ParseLocationListener getParseLocationListener() {
		return locationListener;
	}

	public void setParserConfig(ParserConfig config) {
		setVerifyData(config.verifyData());
		setStopAtFirstError(config.stopAtFirstError());
		setDatatypeHandling(config.datatypeHandling());
		setPreserveBNodeIDs(config.isPreserveBNodeIDs());
	}

	public ParserConfig getParserConfig() {
		return new ParserConfig(verifyData(), stopAtFirstError(),
				preserveBNodeIDs(), datatypeHandling());
	}

	public void setVerifyData(boolean verifyData) {
		this.verifyData = verifyData;
	}

	public boolean verifyData() {
		return verifyData;
	}

	public void setPreserveBNodeIDs(boolean preserveBNodeIDs) {
		this.preserveBNodeIDs = preserveBNodeIDs;
	}

	public boolean preserveBNodeIDs() {
		return preserveBNodeIDs;
	}

	public void setStopAtFirstError(boolean stopAtFirstError) {
		this.stopAtFirstError = stopAtFirstError;
	}

	public boolean stopAtFirstError() {
		return stopAtFirstError;
	}

	public void setDatatypeHandling(DatatypeHandling datatypeHandling) {
		this.datatypeHandling = datatypeHandling;
	}

	public DatatypeHandling datatypeHandling() {
		return datatypeHandling;
	}

	/**
	 * Parses and normalizes the supplied URI-string and sets it as the base URI
	 * for resolving relative URIs.
	 */
	protected void setBaseURI(String uriSpec) {
		// Store normalized base URI
		ParsedURI baseURI = new ParsedURI(uriSpec);
		baseURI.normalize();
		setBaseURI(baseURI);
	}

	/**
	 * Sets the base URI for resolving relative URIs.
	 */
	protected void setBaseURI(ParsedURI baseURI) {
		this.baseURI = baseURI;
	}

	/**
	 * Associates the specified prefix to the specified namespace.
	 */
	protected void setNamespace(String prefix, String namespace) {
		namespaceTable.put(prefix, namespace);
	}

	/**
	 * Gets the namespace that is associated with the specified prefix or throws
	 * an {@link RDFParseException}.
	 * 
	 * @throws RDFParseException
	 *             if no namespace is associated with this prefix
	 */
	protected String getNamespace(String prefix) throws RDFParseException {
		if (namespaceTable.containsKey(prefix))
			return namespaceTable.get(prefix);
		String msg = "Namespace prefix '" + prefix + "' used but not defined";
		if (defaultPrefix.containsKey(prefix)) {
			reportError(msg);
			return defaultPrefix.get(prefix);
		} else if ("".equals(prefix)) {
			msg = "Default namespace used but not defined";
		}
		reportFatalError(msg);
		throw new RDFParseException(msg);
	}

	/**
	 * Clears any information that has been collected while parsing. This method
	 * must be called by subclasses when finishing the parse process.
	 */
	protected void clear() {
		baseURI = null;
		clearBNodeIDMap();
		namespaceTable.clear();
	}

	/**
	 * Clears the map that keeps track of blank nodes that have been parsed.
	 * Normally, this map is clear when the document has been parsed completely,
	 * but subclasses can clear the map at other moments too, for example when a
	 * bnode scope ends.
	 */
	protected void clearBNodeIDMap() {
		bNodeIDMap.clear();
	}

	/**
	 * Resolves a URI-string against the base URI and creates a {@link URI}
	 * object for it.
	 */
	protected URI resolveURI(String uriSpec) throws RDFParseException {
		// Resolve relative URIs against base URI
		ParsedURI uri = new ParsedURI(uriSpec);

		if (uri.isRelative()) {
			if (baseURI == null) {
				reportFatalError("Unable to resolve URIs, no base URI has been set");
			}

			if (verifyData) {
				if (uri.isRelative() && !uri.isSelfReference()
						&& baseURI.isOpaque()) {
					reportError("Relative URI '"
							+ uriSpec
							+ "' cannot be resolved using the opaque base URI '"
							+ baseURI + "'");
				}
			}

			uri = baseURI.resolve(uri);
		}

		return createURI(uri.toString());
	}

	/**
	 * Creates a {@link URI} object for the specified URI-string.
	 */
	protected URI createURI(String uri) throws RDFParseException {
		try {
			return valueFactory.createURI(uri);
		} catch (Exception e) {
			reportFatalError(e);
			return null; // required by compiler
		}
	}

	/**
	 * Creates a new {@link BNode} object.
	 */
	protected BNode createBNode() throws RDFParseException {
		try {
			return valueFactory.createBNode();
		} catch (Exception e) {
			reportFatalError(e);
			return null; // required by compiler
		}
	}

	/**
	 * Creates a {@link BNode} object for the specified identifier.
	 */
	protected BNode createBNode(String nodeID) throws RDFParseException {
		// Maybe the node ID has been used before:
		BNode result = bNodeIDMap.get(nodeID);

		if (result == null) {
			// This is a new node ID, create a new BNode object for it
			try {
				if (preserveBNodeIDs) {
					result = valueFactory.createBNode(nodeID);
				} else {
					result = valueFactory.createBNode();
				}
			} catch (Exception e) {
				reportFatalError(e);
			}

			// Remember it, the nodeID might occur again.
			bNodeIDMap.put(nodeID, result);
		}

		return result;
	}

	/**
	 * Creates a {@link Literal} object with the supplied parameters.
	 */
	protected Literal createLiteral(String label, String lang, URI datatype)
			throws RDFParseException {
		if (datatype != null) {
			if (verifyData && datatypeHandling != DatatypeHandling.IGNORE) {
				if (!XMLDatatypeUtil.isBuiltInDatatype(datatype)) {
					// report a warning on all unrecognized datatypes
					if (datatype.stringValue().startsWith("xsd")) {
						reportWarning("datatype '"
								+ datatype
								+ "' seems be a prefixed name, should be a full URI instead.");
					} else {
						reportWarning("'"
								+ datatype
								+ "' is not recognized as a supported xsd datatype.");
					}
				}
			}

			if (datatypeHandling == DatatypeHandling.VERIFY) {
				if (!XMLDatatypeUtil.isValidValue(label, datatype)) {
					reportError("'" + label
							+ "' is not a valid value for datatype " + datatype);
				}
			} else if (datatypeHandling == DatatypeHandling.NORMALIZE) {
				try {
					label = XMLDatatypeUtil.normalize(label, datatype);
				} catch (IllegalArgumentException e) {
					reportError("'" + label
							+ "' is not a valid value for datatype " + datatype
							+ ": " + e.getMessage());
				}
			}
		}

		try {
			if (datatype != null) {
				return valueFactory.createLiteral(label, datatype);
			} else if (lang != null) {
				if (verifyData()) {
					if (!isValidLanguageTag(lang)) {
						reportError("'" + lang
								+ "' is not a valid language tag ");
					}
				}
				return valueFactory.createLiteral(label, lang);
			} else {
				return valueFactory.createLiteral(label);
			}
		} catch (Exception e) {
			reportFatalError(e);
			return null; // required by compiler
		}
	}

	/**
	 * Checks that the supplied language tag conforms to lexical formatting as
	 * specified in RFC-3066.
	 * 
	 * @see <a href="http://www.ietf.org/rfc/rfc3066.txt">RFC 3066</a>
	 * @param languageTag
	 * @return true if the language tag is lexically valid, false otherwise.
	 */
	protected boolean isValidLanguageTag(String languageTag) {
		// language tag is RFC3066-conformant if it matches this regex:
		// [a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*
		boolean result = Pattern.matches("[a-zA-Z]{1,8}(-[a-zA-Z0-9]{1,8})*",
				languageTag);

		return result;
	}

	/**
	 * Creates a new {@link Statement} object with the supplied components.
	 */
	protected Statement createStatement(Resource subj, URI pred, Value obj)
			throws RDFParseException {
//		System.out.println(valueFactory.getClass().getName());
		try {
			return valueFactory.createStatement(subj, pred, obj);
		} catch (Exception e) {
			reportFatalError(e);
			return null; // required by compiler
		}
	}

	/**
	 * Creates a new {@link Statement} object with the supplied components.
	 */
	protected Statement createStatement(Resource subj, URI pred, Value obj,
			Resource context) throws RDFParseException {
		try {
			return valueFactory.createStatement(subj, pred, obj, context);
		} catch (Exception e) {
			reportFatalError(e);
			return null; // required by compiler
		}
	}

	/**
	 * Reports the specified line- and column number to the registered
	 * {@link ParseLocationListener}, if any.
	 */
	protected void reportLocation(int lineNo, int columnNo) {
		if (locationListener != null) {
			locationListener.parseLocationUpdate(lineNo, columnNo);
		}
	}

	/**
	 * Reports a warning to the registered ParseErrorListener, if any. This
	 * method simply calls {@link #reportWarning(String,int,int)} supplying
	 * <tt>-1</tt> for the line- and column number.
	 */
	protected void reportWarning(String msg) {
		reportWarning(msg, -1, -1);
	}

	/**
	 * Reports a warning with associated line- and column number to the
	 * registered ParseErrorListener, if any.
	 */
	protected void reportWarning(String msg, int lineNo, int columnNo) {
		if (errListener != null) {
			errListener.warning(msg, lineNo, columnNo);
		}
	}

	/**
	 * Reports an error to the registered ParseErrorListener, if any. This
	 * method simply calls {@link #reportError(String,int,int)} supplying
	 * <tt>-1</tt> for the line- and column number. This method throws a
	 * <tt>ParseException</tt> when 'stop-at-first-error' has been set to
	 * <tt>true</tt>.
	 * 
	 * @see #setStopAtFirstError
	 */
	protected void reportError(String msg) throws RDFParseException {
		reportError(msg, -1, -1);
	}

	/**
	 * Reports an error with associated line- and column number to the
	 * registered ParseErrorListener, if any. This method throws a
	 * <tt>ParseException</tt> when 'stop-at-first-error' has been set to
	 * <tt>true</tt>.
	 * 
	 * @see #setStopAtFirstError
	 */
	protected void reportError(String msg, int lineNo, int columnNo)
			throws RDFParseException {
		if (errListener != null) {
			errListener.error(msg, lineNo, columnNo);
		}

		if (stopAtFirstError) {
			throw new RDFParseException(msg, lineNo, columnNo);
		}
	}

	/**
	 * Reports a fatal error to the registered ParseErrorListener, if any, and
	 * throws a <tt>ParseException</tt> afterwards. This method simply calls
	 * {@link #reportFatalError(String,int,int)} supplying <tt>-1</tt> for the
	 * line- and column number.
	 */
	protected void reportFatalError(String msg) throws RDFParseException {
		reportFatalError(msg, -1, -1);
	}

	/**
	 * Reports a fatal error with associated line- and column number to the
	 * registered ParseErrorListener, if any, and throws a
	 * <tt>ParseException</tt> afterwards.
	 */
	protected void reportFatalError(String msg, int lineNo, int columnNo)
			throws RDFParseException {
		if (errListener != null) {
			errListener.fatalError(msg, lineNo, columnNo);
		}

		throw new RDFParseException(msg, lineNo, columnNo);
	}

	/**
	 * Reports a fatal error to the registered ParseErrorListener, if any, and
	 * throws a <tt>ParseException</tt> afterwards. An exception is made for the
	 * case where the supplied exception is a {@link RDFParseException}; in that
	 * case the supplied exception is not wrapped in another ParseException and
	 * the error message is not reported to the ParseErrorListener, assuming
	 * that it has already been reported when the original ParseException was
	 * thrown.
	 * <p>
	 * This method simply calls {@link #reportFatalError(Exception,int,int)}
	 * supplying <tt>-1</tt> for the line- and column number.
	 */
	protected void reportFatalError(Exception e) throws RDFParseException {
		reportFatalError(e, -1, -1);
	}

	/**
	 * Reports a fatal error with associated line- and column number to the
	 * registered ParseErrorListener, if any, and throws a
	 * <tt>ParseException</tt> wrapped the supplied exception afterwards. An
	 * exception is made for the case where the supplied exception is a
	 * {@link RDFParseException}; in that case the supplied exception is not
	 * wrapped in another ParseException and the error message is not reported
	 * to the ParseErrorListener, assuming that it has already been reported
	 * when the original ParseException was thrown.
	 */
	protected void reportFatalError(Exception e, int lineNo, int columnNo)
			throws RDFParseException {
		if (e instanceof RDFParseException) {
			throw (RDFParseException) e;
		} else {
			if (errListener != null) {
				errListener.fatalError(e.getMessage(), lineNo, columnNo);
			}

			throw new RDFParseException(e, lineNo, columnNo);
		}
	}

}
