/*
* Support for k.Actors. Lots to do.
*/

import { NUMERIC } from "./lib/java.js";

/**
 * performs a shallow merge of multiple objects into one
 *
 * @template T
 * @param {T} original
 * @param {Record<string,any>[]} objects
 * @returns {T} a single new object
 */
 function inherit(original, ...objects) {
  /** @type Record<string,any> */
  const result = Object.create(null);

  for (const key in original) {
    result[key] = original[key];
  }
  objects.forEach(function(obj) {
    for (const key in obj) {
      result[key] = obj[key];
    }
  });
  return /** @type {T} */ (result);
}

export default function(hljs) {
  var JAVA_IDENT_RE = '[\u00C0-\u02B8a-zA-Z_$][\u00C0-\u02B8a-zA-Z_$0-9]*';
  var GENERIC_IDENT_RE = JAVA_IDENT_RE + '(<' + JAVA_IDENT_RE + '(\\s*,\\s*' + JAVA_IDENT_RE + ')*>)?';

  var KEYWORDS = '__KEYWORDS__';
  var QUALITIES = '__CONCRETE_QUALITIES__';
  var SUBJECTS = '__CONCRETE_SUBJECTS__';
  var PROCESSES = '__CONCRETE_PROCESSES__';
  var RELATIONSHIPS = '__CONCRETE_RELATIONSHIPS__';
  var PREDICATES = '__CONCRETE_PREDICATES__';
  var EVENTS = '__CONCRETE_EVENTS__';
  var ABSTRACT_QUALITIES = '__ABSTRACT_QUALITIES__';
  var ABSTRACT_SUBJECTS = '__ABSTRACT_SUBJECTS__';
  var ABSTRACT_PROCESSES = '__ABSTRACT_PROCESSES__';
  var ABSTRACT_RELATIONSHIPS = '__ABSTRACT_RELATIONSHIPS__';
  var ABSTRACT_PREDICATES = '__ABSTRACT_PREDICATES__';
  var ABSTRACT_EVENTS = '__ABSTRACT_EVENTS__';
  var CONFIGURATIONS = "__CONCRETE_CONFIGURATIONS__";
  var ABSTRACT_CONFIGURATIONS = "__ABSTRACT_CONFIGURATIONS__";
  var VIEW_VERBS = "__VIEW_VERBS__";
  var USER_VERBS = "__USER_VERBS__";
  var OBJECT_VERBS = "__USER_VERBS__";
  var STATE_VERBS = "__STATE_VERBS__";
  var SESSION_VERBS = "__SESSION_VERBS__";
  var EXPLORER_VERBS = "__EXPLORER_VERBS__";
  var IMPORTED_VERBS = "__IMPORTED_VERBS__";

 var ANNOTATION = {
    className: 'meta',
    begin: '@' + JAVA_IDENT_RE,
    contains: [
      {
        begin: /\(/,
        end: /\)/,
        contains: ["self"] // allow nested () inside our annotation
      },
    ]
  };
  const NUMBER = NUMERIC;

  const TEXTBLOCK = function(begin, end, modeOptions = {}) {
    const mode = hljs.inherit(
      {
        className: 'textblock',
        begin,
        end,
        contains: []
      },
      modeOptions
    );
    return mode;
  };

  return {
    name: 'KActors',
    aliases: ['kactors'],
    keywords: {
		  // admits ns.subns:Concept notation
		  $pattern: /\b[a-z\.]+(:[A-Z][A-z]+)?\b/,
		  keyword: KEYWORDS,
		  viewverb: VIEW_VERBS,
		  userverb: USER_VERBS,
		  objectverb: OBJECT_VERBS,
		  stateverb: STATE_VERBS,
		  sessionverb: SESSION_VERBS,
		  explorerverb: EXPLORER_VERBS,
		  importedverb: IMPORTED_VERBS,
      	  quality: QUALITIES,
		  predicate: PREDICATES,
		  subject: SUBJECTS,
		  process: PROCESSES,
          event: EVENTS,
		  relationship: RELATIONSHIPS,
          configuration: CONFIGURATIONS,
		  aquality: ABSTRACT_QUALITIES,
		  apredicate: ABSTRACT_PREDICATES,
		  asubject: ABSTRACT_SUBJECTS,
		  aprocess: ABSTRACT_PROCESSES,
		  arelationship: ABSTRACT_RELATIONSHIPS,
		  aevent: ABSTRACT_EVENTS,
          aconfiguration: ABSTRACT_CONFIGURATIONS
	  },
    illegal: /<\/|#/,
    contains: [
      hljs.COMMENT(
        '/\\*\\*',
        '\\*/',
        {
          relevance: 0,
          contains: [
            {
              // eat up @'s in emails to prevent them to be recognized as doctags
              begin: /\w+@/, relevance: 0
            },
            {
              className: 'doctag',
              begin: '@[A-Za-z]+'
            }
          ]
        }
      ),
      // // relevance boost
      // {
      //   begin: /namespace/,
      //   keywords: "namespace",
      //   relevance: 2
      // },
      hljs.C_LINE_COMMENT_MODE,
      hljs.C_BLOCK_COMMENT_MODE,
      hljs.APOS_STRING_MODE,
      hljs.QUOTE_STRING_MODE,
      TEXTBLOCK('%%%', '%%%'),
      // {
      //   className: 'class',
      //   beginKeywords: 'class interface enum', end: /[{;=]/, excludeEnd: true,
      //   relevance: 1,
      //   keywords: 'class interface enum',
      //   illegal: /[:"\[\]]/,
      //   contains: [
      //     { beginKeywords: 'extends implements' },
      //     hljs.UNDERSCORE_TITLE_MODE
      //   ]
      // },
      // try to catch klab namespace
      {
        className: 'namespace',
        beginKeywords: 'namespace', 
        end: /[{;=]/, 
        excludeEnd: true,
        relevance: 1,
        keywords: 'namespace',
        illegal: /[:"\[\]]/,
        contains: [
          { beginKeywords: 'using' },
          hljs.UNDERSCORE_TITLE_MODE
        ]
      },
      {
        // Expression keywords prevent 'keyword Name(...)' from being
        // recognized as a function definition
        beginKeywords: 'new throw return else',
        relevance: 0
      },
      // {
      //   className: 'class',
      //   begin: 'record\\s+' + hljs.UNDERSCORE_IDENT_RE + '\\s*\\(',
      //   returnBegin: true,
      //   excludeEnd: true,
      //   end: /[{;=]/,
      //   keywords: KEYWORDS,
      //   contains: [
      //     { beginKeywords: "record" },
      //     {
      //       begin: hljs.UNDERSCORE_IDENT_RE + '\\s*\\(',
      //       returnBegin: true,
      //       relevance: 0,
      //       contains: [hljs.UNDERSCORE_TITLE_MODE]
      //     },
      //     {
      //       className: 'params',
      //       begin: /\(/, end: /\)/,
      //       keywords: KEYWORDS,
      //       relevance: 0,
      //       contains: [
      //         hljs.C_BLOCK_COMMENT_MODE
      //       ]
      //     },
      //     hljs.C_LINE_COMMENT_MODE,
      //     hljs.C_BLOCK_COMMENT_MODE
      //   ]
      // },
      // {
      //   className: 'function',
      //   begin: '(' + GENERIC_IDENT_RE + '\\s+)+' + hljs.UNDERSCORE_IDENT_RE + '\\s*\\(', returnBegin: true, end: /[{;=]/,
      //   excludeEnd: true,
      //   keywords: KEYWORDS,
      //   contains: [
      //     {
      //       begin: hljs.UNDERSCORE_IDENT_RE + '\\s*\\(', returnBegin: true,
      //       relevance: 0,
      //       contains: [hljs.UNDERSCORE_TITLE_MODE]
      //     },
      //     {
      //       className: 'params',
      //       begin: /\(/, end: /\)/,
      //       keywords: KEYWORDS,
      //       relevance: 0,
      //       contains: [
      //         ANNOTATION,
      //         hljs.APOS_STRING_MODE,
      //         hljs.QUOTE_STRING_MODE,
      //         NUMBER,
      //         hljs.C_BLOCK_COMMENT_MODE
      //       ]
      //     },
      //     hljs.C_LINE_COMMENT_MODE,
      //     hljs.C_BLOCK_COMMENT_MODE
      //   ]
      // },
      NUMBER,
      ANNOTATION
    ]
  };
}
