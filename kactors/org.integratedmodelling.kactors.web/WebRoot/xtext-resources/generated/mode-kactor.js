define(["ace/lib/oop", "ace/mode/text", "ace/mode/text_highlight_rules"], function(oop, mText, mTextHighlightRules) {
	var HighlightRules = function() {
		var keywords = "AD|BC|CE|E|action|adjacent|and|app|as|assert|assessment|author|averaged|behavior|behaviour|by|caused|causing|change|changed|component|contained|containing|count|create|created|def|description|desktop|distance|do|down|during|e|each|else|empty|exception|exclusive|false|follows|for|from|function|identified|identity|if|import|in|inclusive|is|l|level|library|linking|locale|logo|magnitude|minus|mobile|modified|monetary|named|new|no|not|observability|observable|occurrence|of|ok|optional|or|output|over|per|percentage|permissions|plus|presence|probability|proportion|public|rate|ratio|required|script|set|style|summed|task|testcase|times|to|total|trait|true|type|uncertainty|unknown|user|value|version|versionstring|web|where|while|with|within|without|worldview";
		this.$rules = {
			"start": [
				{token: "comment", regex: "\\/\\/.*$"},
				{token: "comment", regex: "\\/\\*", next : "comment"},
				{token: "string", regex: '["](?:(?:\\\\.)|(?:[^"\\\\]))*?["]'},
				{token: "string", regex: "['](?:(?:\\\\.)|(?:[^'\\\\]))*?[']"},
				{token: "constant.numeric", regex: "[+-]?\\d+(?:(?:\\.\\d*)?(?:[eE][+-]?\\d+)?)?\\b"},
				{token: "lparen", regex: "[({]"},
				{token: "rparen", regex: "[)}]"},
				{token: "keyword", regex: "\\b(?:" + keywords + ")\\b"}
			],
			"comment": [
				{token: "comment", regex: ".*?\\*\\/", next : "start"},
				{token: "comment", regex: ".+"}
			]
		};
	};
	oop.inherits(HighlightRules, mTextHighlightRules.TextHighlightRules);
	
	var Mode = function() {
		this.HighlightRules = HighlightRules;
	};
	oop.inherits(Mode, mText.Mode);
	Mode.prototype.$id = "xtext/kactor";
	Mode.prototype.getCompletions = function(state, session, pos, prefix) {
		return [];
	}
	
	return {
		Mode: Mode
	};
});
