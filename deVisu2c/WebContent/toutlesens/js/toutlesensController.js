/**************************  TOUTLESENS LICENCE*************************

The MIT License (MIT)

Copyright (c) 2016 Claude Fauconnet claude.fauconnet@neuf.fr

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

**********************************************************************/

//http://graphaware.com/neo4j/2015/01/16/neo4j-graph-model-design-labels-versus-indexed-properties.html

var traversalToTree = true;
var traversalToGraph = false;
var traversalToSpredsheet = false;
var d3tree;
var spreadsheet;
var currentLabel;
/*
 * var timeSlider; var initEventTypes;
 */

var page = 0;
var size = 100;
var sep = "\t";

var maxSpreadsheetRows = 998;

var QUERY_TYPE_MATCH = 0;
var QUERY_TYPE_LABELS = 1;
var QUERY_TYPE_TRAVERSAL = 2;
var QUERY_TYPE_GET_ID = 3;
currentQueryType=QUERY_TYPE_MATCH;
var rIndices = 1;
var currentObject;
var currentObjId;

var nodeTypes = [];
var popopuPosition = {
	x : 0,
	y : 0
};

var oldData = [];

var addToOldData = true;
var oldTreeRootNode;
var treeSelectedNode;
var treeLevel = 1;
var infoDisplayMode = "PANEL";// "POPUP";

var subGraph;
var queryParams={};





var palette=['#F2DADA',
         '#D7D7DD',
         '#CCCDFF',
         '#BBDDBB',
         '#D8ADAE',
         '#999BFF',
         '#F2797C',
         '#8888AA',
         '#557755',
         '#118811',
         '#7F3F41',
         '#992222']
var nodeColors = {}

var green = "green";
var blue = "blue";
var red = "red";

var labelsPositions = {};
var initialQuery = "";
var currentVariables = [];
var currentVariable = "";
var selectedObject = {};
var subGraph;
var d3tree;




$(document).ready(function() {
	queryParams = getQueryParams(document.location.search);
	 subGraph=queryParams.subGraph;
	$("#tabs").tabs();
	$(".fillScreenHeight").css("bottom", 600);

	getDBModel();
	initLabels();
	initChronology();
	initResponsive();
	getProperties2();


	
	


});

function getQueryParams(qs) {
	
	qs = qs.split("+").join(" ");

	var params = {}, tokens, re = /[?&]?([^=]+)=([^&]*)/g;

	while (tokens = re.exec(qs)) {
		params[decodeURIComponent(tokens[1])] = decodeURIComponent(tokens[2]);
	}

	return params;
}


function initResponsive(){


// var graphWidth=$("#bottomPanel").width();
// var graphHeight= window.innerHeight-200;
// $("#graphContainerDiv").width(graphWidth-300);
// $("#infoPanel").width(300);
// $("#graphContainerDiv").height(graphHeight);
// $("#infoPanel").height(graphHeight);
}


function initLabels(){

	whereClause="";
	if(subGraph)
		whereClause='where n.subGraph="'+subGraph+'"';
	var payload = {
			"statements" : [ {
				"statement" : "MATCH (n) "+whereClause+" return distinct labels(n)"
			} ]
		};
	
	paramsObj = {
		mode : "POST",
		urlSuffix : "db/data/transaction/commit",
		payload : JSON.stringify(payload)
	}
/*	paramsObj = {
			mode : "GET",
			urlSuffix : "db/data/labels",

		}*/
	$.ajax({
		type : "POST",
		url : "neoapi",
		data : paramsObj,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {

			var errors = data.errors;
			clipboardNode
			if (errors && errors.length > 0) {
				var str = "ERROR :";
				for (var i = 0; i < errors.length; i++) {
					str += errors[i].code + " : " + errors[i].message + "<br>"
							+ JSON.stringify(paramsObj);
				}
				setMessage(str, red);
				return;
			}
			doLoadLabels(data);
		}
			,
			error : function(xhr, err, msg) {
				console.log(xhr);
				console.log(err);
				console.log(msg);
			},
			
			
		
			
		
		});
	
	
}

function doLoadLabels(data) {

	var results=data.results[0].data;
	if(results.length==0)
		return;

		var labels=[];
	for(i=0;i<results.length;i++){
		labels.push(results[i].row[0]);
		
	}
	labels.sort();
	nodeTypes = labels;
	for (var i = 0; i < labels.length; i++) {
		if (i < palette.length)
			nodeColors[labels[i]] = palette[i];
		else
			nodeColors[labels[i]] = "gray";

		var str = labels[i];
		$("#nodesLabelsSelect").append($('<option>', {
			text : str,
			value : str
		}).css("color", palette[i]));

	}
}

function doLoadRelationshipTypes(labels) {
	for (var i = 0; i < labels.length; i++) {
		var str = labels[i];
		$("#linksLabelsSelect").append($('<option>', {
			text : str,
			value : str
		}));

	}
	
	doLoadLabels
}

function onNodesLabelsSelect(select) {

	var label = $(select).val();
	var variable = label;// .toLowerCase();
	var query = $("#queryTA").val();
	if (query == "") {
		rIndices = 0;
		labelsPositions = {};
		query = "MATCH (" + variable + ":" + label + ") return " + variable;
		$("#queryTA").val(query);
	} else {
		rIndices++;doLoadLabels
		var p = query.indexOf(" return");
		var returnStr = query.substring(p);
		query = query.substring(0, p) + "-[r" + rIndices + "]-(" + variable
				+ ":" + label + ")" + returnStr + "," + variable;
		$("#queryTA").val(query);
	}
	var labelPosition = query.indexOf(":" + label) + label.length + 1;
	labelsPositions[variable] = labelPosition;
	initialQuery = query;
	$(select).val("");

}
function onLabelSelect(labelSelect) {
	var mode = $("#outputModeHome:checked").val();
	if (mode == "CHRONOCHART") {
		drawChronoChart();
	} else {
		searchNodes(null, labelSelect);
	}

}

function onMatchGo() {

	executeQuery(QUERY_TYPE_MATCH, $("#queryTA").val(), null)
}

function onTraversalGo(json) {

	return executeQue.ry(QUERY_TYPE_GET_ID, json, set)
}

function setQueryId(node) {
	var query = initialQuery;
	if (node) {
		currentVariable = node.type;

		var p = labelsPositions[node.type];
		query = query.substring(0, p) + "{id:" + node.id + "}"
				+ query.substring(p);
	}
	$("#queryTA").val(query);
	executeQuery(QUERY_TYPE_MATCH, query, toGraph);
}

// function nodesLabelsSelect" size="7" onchange="onNodesLabelsSelect()">

function executeQuery(queryType, str, successFunction) {
	currentQueryType=queryType;
	var www = $("#limitResult");
	if (str && str.toLowerCase().indexOf('limit') < 0 && str.toLowerCase().indexOf('delete')<0)
		str += " limit " + $("#limitResult").val();
	nameLength = 30; // 0; parseInt($("#labelLength").val());
	var paramsObj = {};

	var urlSuffix = "";
	var params = "";

	if (queryType == QUERY_TYPE_MATCH) {
		var payload = {
			"statements" : [ {
				"statement" : str
			} ]
		};
		paramsObj = {
			mode : "POST",
			urlSuffix : "db/data/transaction/commit",
			payload : JSON.stringify(payload)
		}


	} else if (queryType == QUERY_TYPE_TRAVERSAL) {

		var payload = {
			"order" : "breadth_first",
			"max_depth" : parseInt($("#traversalDepth").val()),
			"uniqueness" : "relationship_global",
			"return_filter" : {
				"body" : $("#traversalFilter").val(),
				"language" : "javascript"
			}
		}

		paramsObj = {
			mode : "POST",
			urlSuffix : "db/data/node/" + str + "/traverse/path",
			payload : JSON.stringify(payload),
				type : "POST",
				url : "neoapi",
				data : paramsObj,
				dataType : "json",
		}
	}
	else{
		
	}
				
		

	

	
console.log("QUERY----"+JSON.stringify(payload));
	$.ajax({
		type : "POST",
		url : "neoapi",
		data : paramsObj,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {

			if (!data || data.length == 0) {
				setMessage("No results", blue);
				return;
			}
			var errors = data.errors;

			if (errors && errors.length > 0) {
				var str = "ERROR :";
				for (var i = 0; i < errors.length; i++) {
					str += errors[i].code + " : " + errors[i].message + "<br>"
							+ JSON.stringify(paramsObj);
				}
				setMessage(str, red);
				return;
			}
			
		
			 if ($.isArray(data)) {// labels...
				if (successFunction) {
					successFunction(data);
					return;
				} else
					return data;
			}

			var results = data.results;

			if (results && results.length > 0){//} && results[0].data.length > 0) {// match..
				completeResult(results);
				if (successFunction) {
					successFunction(results);
					return;
				} else {
					return results;
				}

			} else {

				setMessage("No results", blue);
				cleanTabDivs();
				return -1;
			}

		},
		error : function(xhr, err, msg) {
			console.log(xhr);
			console.log(err);
			console.log(msg);
		},

	});

}

function cleanTabDivs() {
	// $("#spreadSheetDiv").html("");
	$("#graphDiv").html("");
	$("#resultX").html("");
}

function toCsv(neoResult) {
	var str = JSON.stringify(neoResult);
	$("#resultX").html(str);

}

function completeResult(neoResult) {

	var data = neoResult[0].data;
	for (var i = 0; i < data.length; i++) {
		var row = data[i].row;
		if (row.length < 6)
			return;
		for (var j = 0; j < 3; j++) {

			row[j].id = row[j + 5];
			if (!row[j].name)
				row[j].name = row[j].nom;

		}
		if (!row[0].type)
			row[0].type = row[3][0];
		if (!row[1].type)
			row[1].type = row[4][0];

	}

	/*
	 * if (addToOldData && !AutoDisplayTree) { neoResult[0].data =
	 * oldData.concat(data); }
	 */
	oldData = neoResult[0].data;

}

var recursiveBuildTreeOccurences = {};
function recursiveBuildTree(relations, parentNode) {

	for (var i = 0; i < data.length; i++) {
		if (relations[i].row[0].id == parentNode.id_) {
			var obj = relations[i].row[1];
			if (!parentNode.children)
				parentNode.children = [];
			var childNode = {
				name : obj.name,
				label : obj.name,
				id_ : obj.id,
				type : obj.type

			}
			var occurences = recursiveBuildTreeOccurences[obj.type + "_"
					+ obj.id];
			if (occurences) {// on clone le noeud en incrementant la valeur
				// de name

				recursiveBuildTreeOccurences[obj.type + "_" + obj.id] = occurences++
				childNode.name += "-" + occurences;
				parentNode.children.push(childNode);

			} else {
				recursiveBuildTreeOccurences[obj.type + "_" + obj.id] = 1;

				parentNode.children.push(childNode);
				recursiveBuildTree(relations, childNode);
			}

		}

	}

}

function flattenRelations(relations) {
	var cols = {};
	for (var i = 0; i < relations.length; i++) {
		var obj0 = relations[i].row[0];
		var obj1 = relations[i].row[1];
		if (!cols[obj0.type])
			cols[obj0.type] = [];
		if (!cols[obj1.type])
			cols[obj1.type] = [];
	}

	for (var i = 0; i < relations.length; i++) {
		var obj0 = relations[i].row[0];

		var obj1 = relations[i].row[1];
		for ( var col in cols) {
			if (obj0.type == col) {
				cols[col].push({
					row : i,
					name : obj0.name
				});

			}
			if (obj1.type == col) {
				cols[col].push({
					row : i,
					name : obj1.name
				});
			}
		}

	}

	// console.log(JSON.stringify(cols));

	var array = [];

	for (var i = 0; i < relations.length; i++) {
		var row = {}
		for ( var col in cols) {
			if (cols[col][0].row == i)
				row[col] = cols[col][0].name;
			else if (cols[col][1].row == i)
				row[col] = cols[col][1].name;
			else
				row[col] = "";
		}
		array.push(row);

	}

	return array;
	// console.log(JSON.stringify(array));

}

function upDateGraphFromSpreadsheet(event, coord, obj) {
	var str = $("#spreadSheetDiv").handsontable('getColHeader', coord.col);
	var p = str.indexOf("_");
	var label = str.substring(0, p);
	var property = str.substring(p + 1);
	spreadSheetSelectedObject = {};
	var node = {};
	var obj2 = spreadsheet.getSelectedObject(coord.row);
	// node[currentVariables[index]+"_id"] =obj[currentVariables[index]+"_id"] ;
	selectedObject.id = obj2[label + "_id"];
	selectedObject.type = label;
	executeQuery(QUERY_TYPE_MATCH, " Match (n:" + selectedObject.type + "{id:"
			+ selectedObject.id + "}) return id(n)", function(result) {
		selectedObject.neoId = result[0].data[0].row[0];
		setMessage("SELECTION : " + JSON.stringify(selectedObject), blue);
		if (event.ctrlKey) {
			setQueryId(selectedObject);

		} else {
			if (graph)
				graph.highlightLinkedNodesExt(selectedObject);
		}
	});

}

function storeQuery() {
	if (typeof (Storage) !== "undefined") {
		var name = prompt("request name");
		if (!name || name.length == 0)
			return;
		var query = str = $("#queryTA").val();
		localStorage.setItem(name, query);
		loadStoredQueriesNames();

	} else {
		setMessage("Sorry! No Web Storage support..", red);

	}
}

function loadStoredQueriesNames() {
	if (typeof (Storage) !== "undefined") {
		var queries = []
		for (var i = 0; i < localStorage.length; i++) {
			var queryName = localStorage.key(i);
			$("#storedQueries").append($('<option>', {
				text : queryName,
				value : queryName
			}));
		}

	} else {
		setMessage("Sorry! No Web Storage support..", red);

	}

}

function loadQuery(select) {
	var qName = $(select).val();
	var query = localStorage.getItem(qName);
	$("#queryTA").val(query);
}

function deleteQuery() {
	if (confirm("Are you sure ?")) {
		var qName = $("#storedQueries").val();
		var query = localStorage.removeItem(qName);
		$("#queryTA").val(query);
	}
}

function exportQueries() {
	if (typeof (Storage) !== "undefined") {
		var queries = [];
		for (var i = 0; i < localStorage.length; i++) {
			var queryName = localStorage.key(i);
			var queryValue = localStorage.getItem(queryName);
			var obj = {};
			obj[queryName] = queryValue;
			queries.push(obj);
		}

		var str = JSON.stringify(queries);
		$("#spreadSheetDiv").css("height", "0px");
		$("#csvDiv").css("height", "600px");
		$("#csvText").val(str);

	} else {
		setMessage("Sorry! No Web Storage support..", red);

	}

}

function importQueries() {
	if (typeof (Storage) !== "undefined") {
		var str = prompt("enter exported queries");
		if (!str || str.length == 0)
			return;
		var json = JSON.parse(str);
		nodes
		for (var i = 0; i < json.length; i++) {
			for ( var key in json[i]) {
				localStorage.setItem(key, json[i][key]);
			}
		}
		loadStoredQueriesNames();

	} else {
		setMessage("Sorry! No Web Storage support..", red);

	}

}

function setMessage(str, color) {
	if (color)
		$("#message").css("color", color);

	$("#message").html(str);
}

function selectTab(index) {
	$('#tabs').tabs({
		active : index
	});
}

function clearQuery() {
	$("#queryTA").val("");
}

function searchNodes(wordInput, labelSelect) {
	var word = ".*"
	if (wordInput) {
		wordsSelect.options.length = 0;
		word = $(wordInput).val();
		if (word && word.length < 2) {
			return;
		}
	}

	var str = "";
	
	var  subGraphWhere="";
	if(subGraph)
		subGraphWhere=" and n.subGraph='"+subGraph+"' ";
	
	if (labelSelect) {
		label = $(labelSelect).val();
		if(!label)
			return;
		
		str = "MATCH (n:" + label + ")  WHERE n.nom =~ '(?i).*" + word
				+ ".*'  "+subGraphWhere+" RETURN n,id(n),labels(n)";
	} else {
		str = "MATCH (n)   WHERE n.nom =~ '(?i).*" + word
				+ ".*' "+subGraphWhere+ " RETURN n,id(n),labels(n)";
	}
	var payload = {
		"statements" : [ {
			"statement" : str
		} ]
	};
	paramsObj = {
		mode : "POST",
		urlSuffix : "db/data/transaction/commit",
		payload : JSON.stringify(payload)
	}
	$.ajax({
		type : "POST",
		url : "neoapi",
		data : paramsObj,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {
			if (!data || data.length == 0) {
				setMessage("No results", blue);
				return;
			}
			var errors = data.errors;

			if (errors && errors.length > 0) {
				var str = "ERROR :";
				for (var i = 0; i < errors.length; i++) {
					str += errors[i].code + " : " + errors[i].message + "<br>"
							+ JSON.stringify(paramsObj);
				}
				setMessage("!!! erreur de requete", red);
				console.log(str);
				return;
			}
			fillWordsSelect(data.results);

		},
		error : function(xhr, err, msg) {
			console.log(xhr);
			console.log(err);
			console.log(msg);
		},

	});

	;

}

function fillWordsSelect(neoResult) {
	var xxx = neoResult[0];
	var data = neoResult[0].data;
	var variables = neoResult[0].columns;

	var nodes = {};
	var links = [];
	var index = 0;
	setMessage(data.length + " résultats", green);
	var outData = [ {
		name : "",
		id : -1
	} ];
	for (var i = 0; i < data.length; i++) {
		var row = data[i].row;
		var obj = {
			name : "[" + row[2][0] + "] " + row[0].nom,
			id : row[1],
			label : row[2][0],
			color : nodeColors[row[2][0]]

		}
		outData.push(obj);
	}
	fillSelectOptions(wordsSelect, outData, "name", "id");
}

function fillSelectOptions(select, data, textfield, valueField) {
	select.options.length = 0;
	if (!textfield || !valueField) {
		fillSelectOptionsWithStringArray(select, data);
		return;
	}
	$.each(data, function(i, item) {
		$(select).append($('<option>', {
			text : item[textfield],
			value : item[valueField],

		}));

	});

	var str = $(select).attr("id");
	$.each(data, function(i, item) {
		if (i > 0 && item.color)
			$("#" + str + " option:eq(" + i + ")").css("color", item.color);
	})
}

function onWordSelect(draw) {
	var id = $("#wordsSelect").val();
	var text = $("#wordsSelect option:selected").text();
   currentLabel=text.substring(1,text.indexOf("]"));
	if(!id && currentObject){
		id=currentObject.id;
		text=currentObject.name;
	}

	if (id == -1 && !draw)
		return;
	getGraphDataAroundNode(id);
	var q = text.indexOf("]");
	var label = text.substring(1, q);
	// currentVariable=label;
	addToBreadcrumb({
		id : id,
		label : text,
		color : nodeColors[label]
	});

}

function getGraphDataAroundNode(id, callbackFunction, noRelations) {
	hidePopup();
	if(!id)
		id=currentObjId;
	else
		currentObjId=id;


	var mode = $("#outputModeHome:checked").val();
	$("#bottomPanel").css("visibility", "visible");
	
	if (mode != "CHRONOCHART"){
	$("#chronoParams").css("visibility", "hidden");
	$("#timeSlider").css("visibility", "hidden");
	$("#chronoParams").css("height","0px");
	}
	
	if (mode == "GRAPH2"){
		getNodeAllRelations(id)
		return;
	}
	if (mode == "TREE"){
		getNodeAllRelations(id,"tree");
		return;
	}

	if (!callbackFunction) {
		if (mode == "GRAPH")
			callbackFunction = drawGraph;
		else if (mode == "TREE")
			callbackFunction = drawTree;// drawTreeGroupByLabels;
		else if (mode == "LIST")
			callbackFunction = drawList;
		else if (mode == "SPREADSHEET")
			callbackFunction = drawSpreadsheet;
		else if (mode == "CHRONOCHART") {
			$("#timeSlider").css("visibility", "visible");
			$("#eventTypes").css("visibility", "visible");
			$("#chronoParams").css("visibility", "visible");
			$("#timeSlider").css("visibility", "visible");
			$("#chronoParams").css("height","40px");
		
			$("#graphContainerDiv").css("overflow", "auto");
			drawChronoChart();
			return;
		}
	}
if(!id)
	return;
	var query = "MATCH (n)-[r]-(m) WHERE ID(n) =" + id
			+ " RETURN n,m,r,labels(n),labels(m),ID(n),ID(m) ";
	if(noRelations)
		query = "MATCH (n) WHERE ID(n) =" + id
		+ " RETURN n,'m','r',labels(n),'x',ID(n),'idm' ";
		
	executeQuery(QUERY_TYPE_MATCH, query, callbackFunction);

}

function addToBreadcrumb(obj) {
	var name = obj.label;
	if (obj.label)
		name = obj.name;
	if (obj.name)
		name = obj.nom;
	if (!obj.name)
		return;
	var str = $("#breadcrumb").html()
			+ "&nbsp;&nbsp;<a class='breadcrumb-item' id='bc_" + obj.id
			+ "' href='javascript:getGraphDataAroundNode(" + obj.id + ")'>"
			+ name + "</a>"
	$("#breadcrumb").html(str);
	$(".breadcrumb").css("visibility", "visible");
	var color = nodeColors[obj.type];
	$("#bc_" + obj.id).css("color", color);
}

function hidePopup() {
	$("#popup").css("visibility", "hidden");
}

var showInfos = function(node) {

	getGraphDataAroundNode(node.id, showInfosCallback);

}
function showInfosById(id){
	
	getGraphDataAroundNode(id, showInfosCallback);
}


var showInfosCallback = function(result) {

	var data = result[0].data;

	if (data.length == 0) {
		
	
		
		
		return;
	//	$("#infoPanel").html("no results");

	}

	// ****************************draw
	// properties**********************************
	var obj = data[0].row[0];
	currentObject=obj;
	addToBreadcrumb(obj)
	var str = "";
	var imageBlog;
	var dbPediaInited = false;
	var keysToExclude = [ "id", "name", "imageBlog","subGraph" ];
	var orderedKeys=["type","nom","datedebut","datefin","fonction"];
	
	for(var i=0;i<orderedKeys.length;i++){
		var key=orderedKeys[i];
		if(obj[key])
		str += "<i>" + key + "</i> : " + obj[key] + "<br>";
	}
	
	
	for ( var key in obj) {
		
		
		if (obj["imageBlog"]) {
			imageBlog = obj["imageBlog"];

			generateExternalImg(imageBlog);

		} else {
			if (!dbPediaInited) {
				getDbPediaNotice(obj);
				dbPediaInited = true;
			}

		}
		if (keysToExclude.indexOf(key) < 0 && orderedKeys.indexOf(key)<0)
			str += "<i>" + key + "</i> : " + obj[key] + "<br>";
	}

	var labelsWithImages = [ "MotCle", "Dieu", "Heros", "Dirigeant", "Monstre",
			"SiteArcheologique" ];
	if (labelsWithImages.indexOf(obj.type) > -1) {
		str += "<hr>";
		var tag = obj.nom.sansAccent().toLowerCase();
		var strLinkArticle = "<a target='article' href='http://www.histoiredelantiquite.net/tag/"
				+ tag + "'>articles</a>";
		var strLinkImage = "<a target='images' href='http://www.histoiredelantiquite.net/recherche-de-photos-par-tag/nggallery/tags/"
				+ tag + "'>images</a><br>";

		strLinkArticle = "";
		str += "liens vers le blog de l'antiquite; :" + strLinkArticle
				+ "&nbsp;" + strLinkImage;
	}
	if (infoDisplayMode == "POPUP") {
		$("#popup").html(str);
		$("#popup").css("top", popopuPosition.y);
		$("#popup").css("left", popopuPosition.x);
		$("#popup").css("z-index", 100);
		$("#popup").css("visibility", "visible");
	} else {
		$("#infoPanel").html(str);
		$("#infoPanel").css("visibility", "visible");

	}

	// ****************************draw linked
	// objects**********************************

}

String.prototype.sansAccent = function() {
	var accent = [ /[\300-\306]/g, /[\340-\346]/g, // A, a
	/[\310-\313]/g, /[\350-\353]/g, // E, e
	/[\314-\317]/g, /[\354-\357]/g, // I, i
	/[\322-\330]/g, /[\362-\370]/g, // O, o
	/[\331-\334]/g, /[\371-\374]/g, // U, u
	/[\321]/g, /[\361]/g, // N, n
	/[\307]/g, /[\347]/g, // C, c
	];
	var noaccent = [ 'A', 'a', 'E', 'e', 'I', 'i', 'O', 'o', 'U', 'u', 'N',
			'n', 'C', 'c' ];

	var str = this;
	for (var i = 0; i < accent.length; i++) {
		str = str.replace(accent[i], noaccent[i]);
	}

	return str;
}

function drawList(result) {
	showInfosCallback(result);
	var data = result[0].data;
	if(	data.length==0){
		getGraphDataAroundNode(currentObjId, drawList, true) ;
		return;
	}
	
	if(data[0].row[4]=="x"){// pas de relation
		$("#graphDiv").html("pas de sujets associes");
		return;
	}
	var title = data[0].row[0].name;
	var labels = {};
	for (var i = 0; i < data.length; i++) {
		var row = data[i].row;
		var label = row[4];
		if (!labels[label]) {
			labels[label] = [];
		}
		labels[label].push(row)

	}

	var str = "<B>" + title + "</B>&nbsp;Sujets associes :<ul>";

	for ( var key in labels) {
		str += "<li><B><font color='" + nodeColors[key] + "'> " + key
				+ "</font></B>";
		str += "<ul>";
		var data = labels[key];
		for (var i = 0; i < data.length; i++) {
			var id = data[i][1].id;
			var name = data[i][1].name;
			if (name == key) {
				id = data[i][2].id;
				name = data[i][2].name;
			}
			str += "<li><a href='javascript:getGraphDataAroundNode(" + id
					+ ")'>" + name + "</a></font></li>";
			// console.log(str)
		}
		str += "</ul></li>";
	}
	str += "</ul>"

	$("#graphDiv").html(str);

}

/**
 * **********************************draw 3D
 * ********************************************
 */

function drawTree(neoResult) {
	$("#spreadSheetDiv").css("visibility", "hidden");
	showInfosCallback(neoResult);
	var nameLength = 30;
	var data = neoResult[0].data;
	var variables = neoResult[0].columns;

	if (variables.length < 2)
		return;

	var nodes = {};
	setMessage(data.length + " rows retrieved", green);
	var distinctParentNodes = {};
	var parentsVar = 0;
	if (currentVariable)
		parentsVar = variables.indexOf(currentVariable);
	// selection des noeuds parents
	for (var i = 0; i < data.length; i++) {
		var row = data[i].row;
		var obj = row[parentsVar];
		if (!distinctParentNodes[obj.id]) {
			distinctParentNodes[obj.id] = {
				_id : obj.id + "_" + treeLevel,
				name : obj.name,// + " " + obj.id + "_" + treeLevel,
				type : obj.type,
				children : []
			};
		}
	}
	// remplissages des enfants

	var childrenVar = [];
	for (var i = 0; i < variables.length; i++) {
		if (variables[i] != parentsVar)
			childrenVar.push(i)
	}
	;

	for ( var key in distinctParentNodes) {

		var index = 0;
		for (var i = 0; i < data.length; i++) {
			var row = data[i].row;
			var obj = row[parentsVar];
			if (key == "" + obj.id) {
				// var childObj = row[childrenVar[0]];
				var childObj = row[1];
				distinctParentNodes[key].children.push({
					_id : childObj.id + "_" + treeLevel,
					name : childObj.name,// + " " + childObj.id + "_"+
					// treeLevel,
					type : childObj.type,
					children : []
				})
			}
		}
	}
	var root = {};
	if (oldTreeRootNode && treeSelectedNode) {
		root = oldTreeRootNode;
		var id = treeSelectedNode.id;

		treeSelectedNode = null;
		root2 = findNodeInTree(id, oldTreeRootNode);
		if (!root2)
			return;
		if (root2 && root2.children)
			return;
		root2.children = [];
		var key2;
		for ( var key in distinctParentNodes) {
			for (var i = 0; i < distinctParentNodes[key].children.length; i++) {// on
				// ajoute
				// les
				// nouveaux
				// noeuds
				// à
				// l'ancien
				// arbre
				var newChild = distinctParentNodes[key].children[i];
				var p = newChild._id.indexOf("_");

				var newId = newChild._id.substring(0, p);
				var oldChild = findNodeInTree(newId, oldTreeRootNode);
				if (oldChild) {// si le noeud existe déjà on ne l'ajoute pas
					;// console.log(JSON.stringify(oldChild));
					continue;
				}

				root2.children.push(newChild);
			}
		}

		root = oldTreeRootNode;

	} else {
		/*
		 * var root = {
		 * 
		 * id : -1, isRoot : true, name : "..", // name : "-" + currentVariable,
		 * children : [] } for ( var key in distinctParentNodes) {
		 * 
		 * root.children.push(distinctParentNodes[key]); }
		 */
		for ( var key in distinctParentNodes) {
			var root = distinctParentNodes[key];
		}
	}

	oldTreeRootNode = root;
	treeLevel += 1;
	if (!d3tree)
		d3tree = new D3Tree2($("#graphDiv"));
	d3tree.drawTree(root);
}

function drawTreeGroupByLabels(neoResult) {
	$("#spreadSheetDiv").css("visibility", "hidden");
	showInfosCallback(neoResult);
	var nameLength = 30;
	var data = neoResult[0].data;
	var variables = neoResult[0].columns;

	if (variables.length < 2)
		return;

	var nodes = {};
	setMessage(data.length + " rows retrieved", green);
	var distinctParentNodes = {};
	var parentsVar = 0;
	if (currentVariable)
		parentsVar = variables.indexOf(currentVariable);
	// selection des noeuds parents

	var labelNodes = {};
	for (var i = 0; i < data.length; i++) {
		var row = data[i].row;
		var obj = row[parentsVar];
		if (!distinctParentNodes[obj.id]) {
			distinctParentNodes[obj.id] = {
				_id : obj.id + "_" + treeLevel,
				name : obj.name,// + " " + obj.id + "_" + treeLevel,
				type : obj.type,
				children : []
			};
		}

	}

	// remplissages des enfants

	var childrenVar = [];
	for (var i = 0; i < variables.length; i++) {
		if (variables[i] != parentsVar)
			childrenVar.push(i)
	}
	;

	for ( var key in distinctParentNodes) {

		var index = 0;
		for (var i = 0; i < data.length; i++) {
			var row = data[i].row;
			var obj = row[parentsVar];
			if (key == "" + obj.id) {
				// var childObj = row[childrenVar[0]];
				var childObj = row[1];
				distinctParentNodes[key].children.push({
					_id : childObj.id + "_" + treeLevel,
					name : childObj.name,// + " " + childObj.id + "_"+
					// treeLevel,
					type : childObj.type,
					children : []
				})
			}
			var obj2 = labelNodes[childObj.type];
			if (!obj2) {

				labelNodes[childObj.type] = [];
				var labelNode = {
					id : labelsId--,
					name : key,
					type : key,
					children : [],
				}
				labelNodes[childObj.type].push(labelNode);
			}
			var xxxx = labelNodes[childObj.type];
			var www = labelNodes[childObj.type].children;
			labelNodes[childObj.type].children.push(childObj);
		}
	}
	var root = {};
	if (oldTreeRootNode && treeSelectedNode) {
		root = oldTreeRootNode;
		var id = treeSelectedNode.id;

		treeSelectedNode = null;
		root2 = findNodeInTree(id, oldTreeRootNode);
		if (!root2)
			return;
		if (root2 && root2.children)
			return;
		root2.children = [];
		var key2;
		for ( var key in distinctParentNodes) {
			for (var i = 0; i < distinctParentNodes[key].children.length; i++) {// on
				// ajoute
				// les
				// nouveaux
				// noeuds
				// à
				// l'ancien
				// arbre
				var newChild = distinctParentNodes[key].children[i];
				var p = newChild._id.indexOf("_");

				var newId = newChild._id.substring(0, p);
				var oldChild = findNodeInTree(newId, oldTreeRootNode);
				if (oldChild) {// si le noeud existe déjà on ne l'ajoute pas
					;// console.log(JSON.stringify(oldChild));
					continue;
				}

				root2.children.push(newChild);
			}
		}

		root = oldTreeRootNode;

	} else {
		var root = {

			id : -1,
			isRoot : true,
			name : "..",
			// name : "-" + currentVariable,
			children : []
		}
		var labelsId = -1000;
		for ( var key in labelNodes) {

			root.children.push(labelNodes[key]);
		}

	}

	oldTreeRootNode = root;
	treeLevel += 1;
	if (!d3tree)
		d3tree = new D3Tree2($("#graphDiv"));
	d3tree.drawTree(root);
}
function drawGraph(neoResult) {
	$("#spreadSheetDiv").css("visibility", "hidden");
	showInfosCallback(neoResult);
	var nodes = {};
	var links = [];
	var index = 0;

	var xxx = neoResult[0];
	var data = neoResult[0].data;
	var variables = neoResult[0].columns;

	if (variables.length < 2) {
		setMessage("Graph needs at least 2 node types", "red");
		return;
	}

	setMessage(data.length + " resultats", green);

	for (var i = 0; i < data.length; i++) {
		var row = data[i].row;

		var link = {};
		var sourceId = "_" + row[0].id;
		var targetId = "_" + row[1].id;
		link.source = nodes[sourceId] || (nodes[sourceId] = {
			name : sourceId
		});
		link.target = nodes[targetId] || (nodes[targetId] = {
			name : targetId
		});
		links.push(link);
	}

	for (var i = 0; i < data.length; i++) {
		var row = data[i].row;

		var sourceId = "_" + row[0].id;
		var targetId = "_" + row[1].id;
		if (!row[1].name)
			row[1].name = "xxx";
		if (!row[0].name)
			row[0].name = "xxx";

		if (nodes[sourceId]) {
			nodes[sourceId].type = variables[0];
			nodes[sourceId].abbrev = row[0].name.substring(0, nameLength);
			nodes[sourceId].label = row[0].name;
			nodes[sourceId].id = row[0].id;
		}
		if (nodes[targetId]) {
			nodes[targetId].type = variables[1];
			nodes[targetId].abbrev = row[1].name.substring(0, nameLength);
			nodes[targetId].label = row[1].name;
			nodes[targetId].id = row[1].id;
			if (nodes[sourceId])
				nodes[targetId].parentNode = nodes[sourceId];
		}
		if (row[0].type) {
			nodes[sourceId].type = row[0].type;
		} else {
			nodes[sourceId].type = variables[0];
		}
		if (row[1].type) {
			nodes[targetId].type = row[1].type;
		} else {
			nodes[targetId].type = variables[1];
		}
	}

	graph = new ForceGraph("#graphDiv", nodes, links);
	// graph.cleanGraph();
	if (true || nodes.length > 0) {
		graph.nodeColors = nodeColors;
		graph.drawGraph();
	}

}

function findNodeInTree(id, node) {
	if (node._id && node._id.indexOf(id) == 0)
		return node;
	if (!node.children)
		node.children = [];
	for (var i = 0; i < node.children.length; i++) {
		var child = node.children[i];
		if (child._id && child._id.indexOf(id + "_") == 0) {
			console.log("---" + child.name + "-" + child.id)
			return child;

		}

		else {
			var found = findNodeInTree(id, child);
			if (found)
				return found;
		}

	}
	return null;
}

function removeDoublonNodes(root2, distinctParentNodes) {
	var toRemove = [];
	for (var i = 0; i < distinctParentNodes.length; i++) {
		var node = distinctParentNode[i];
		if (node._id == root2.id)
			toRemove.push(node);
	}

}

function drawSpreadsheet(neoResult) {
	$("#spreadSheetDiv").css("visibility", "visible");
	var rows = [];
	var spreadsheetHeaders = [];
	spreadSheetSelectedObject = {};
	var data = neoResult[0].data;
	var variables = neoResult[0].columns;
	var doublons = [];
	for (var i = 0; i < data.length; i++) {
		var obj = {};
		var rowData = data[i].row;

		for (var j = 0; j < rowData.length; j++) {
			var str = "";
			for ( var key in rowData[j]) {
				str += rowData[j][key]
				obj[variables[j] + "_" + key] = rowData[j][key];
			}

		}
		if (doublons.indexOf(str < 0)) {
			doublons.push(str);
			rows.push(obj);
		} else {
			console.log("doublon detected");
		}
		if (rows.length > maxSpreadsheetRows) {
			setMessage("more than " + maxSpreadsheetRows + " rows")
			break;
		}

	}

	for ( var key in rows[0]) {
		spreadsheetHeaders.push(key);
	}

	if (rows.length <= maxSpreadsheetRows) {
		setMessage(rows.length + " rows retrieved", green)

		spreadsheet = new Spreadsheet("spreadSheetDiv");
		spreadsheet.headers = spreadsheetHeaders;
		spreadsheet.onCellClickCallBack = upDateGraphFromSpreadsheet;
		spreadsheet.load(rows);
	} else {
		$("#popupTextarea").text("aaaaa");
		$("#popupTextarea").style.visibility = "visible";
	}

}

function setMessage(message, color) {
	$("#message").html(message);
	if (color)
		$("#message").css("color", color);
}