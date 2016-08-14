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

var spreadsheet;
var page = 0;
var size = 100;
var sep = "\t";

var maxSpreadsheetRows = 998;

var AutoDisplaySpreadSheet = true;
var AutoDisplayGraph = true;

var AutoDisplayTree = false;

var QUERY_TYPE_MATCH = 0;
var QUERY_TYPE_LABELS = 1;
var QUERY_TYPE_TRAVERSAL = 2;
var QUERY_TYPE_GET_ID = 3;
var rIndices = 1;

var palette = [ "#BF7EFF", "#FF3686", "#07EAF2", "#F2F202", "#4AFF4A", "#2FBF0B", "#0BBAA2", "#DDAABB" ,"#BF7EFF", "#FF3686", "#07EAF2", "#F2F202", "#4AFF4A", "#2FBF0B", "#0BBAA2", "#DDAABB" ,"#BF7EFF", "#FF3686", "#07EAF2", "#F2F202", "#4AFF4A", "#2FBF0B", "#0BBAA2", "#DDAABB" ];
var nodeColors = {};/*{
	Techno : "#BF7EFF",
	DC : "#FF3686",
	UC : "#07EAF2",
	SC : "#F2F202",
	BC : "#4AFF4A",
	BD : "#2FBF0B",
	BU : "#0BBAA2",
	CC : "#DDAABB"

}*/

var green = "green";
var blue = "blue";
var red = "red";

var labelsPositions = {};
var initialQuery = "";
var currentVariables = [];
var currentVariable = "";
var selectedObject = {};
var graph;

$(document).ready(function() {
	 
	$("#tabs").tabs();
	$(".fillScreenHeight").css("bottom", 600);
	setOutputMode();
	executeQuery(QUERY_TYPE_LABELS, null, doLoadLabels);

});

function doLoadLabels(labels) {
	for (var i = 0; i < labels.length; i++) {
		if (i < palette.length)
			nodeColors[labels[i]] = palette[i];
		else
			nodeColors[label] = "gray";
	
		var str = labels[i];
		$("#nodesLabelsSelect").append($('<option>', {
			text : str,
			value : str
		}).css("color",palette[i]));

	}
	labels.sort();

}

function doLoadRelationshipTypes(labels) {
	for (var i = 0; i < labels.length; i++) {
		var str = labels[i];
		$("#linksLabelsSelect").append($('<option>', {
			text : str,
			value : str
		}));

	}

}

function setMatchOutputMode(select) {
	var mode = $(select).val();
	if (mode == "GRAPH") {
		AutoDisplayGraph = true;
		AutoDisplayTree = false;
	} else if (mode == "TREE") {
		AutoDisplayGraph = false;
		AutoDisplayTree = true;
	}
}

function setOutputMode(select) {
	var mode = $(select).val();
	if (mode == "GRAPH") {
		AutoDisplayGraph = true;
		AutoDisplayTree = false;
		AutoDisplaySpreadSheet = false;
		$("#spreadSheetDiv").css("visibility", "hidden");
		$("#graphContainerDiv").css("visibility", "visible");
	} else if (mode == "TREE") {
		AutoDisplayGraph = false;
		AutoDisplayTree = true;
		AutoDisplaySpreadSheet = false;
		$("#spreadSheetDiv").css("visibility", "hidden");
		$("#graphContainerDiv").css("visibility", "visible");

	} else if (mode == "SPREADSHEET") {
		AutoDisplayGraph = false;
		AutoDisplayTree = false;
		AutoDisplaySpreadSheet = true;
		$("#spreadSheetDiv").css("visibility", "visible");
		$("#graphContainerDiv").css("visibility", "hidden");

	}

}

function setOutputMode2(select) {
	var mode = $(select).val();
	if (mode == "GRAPH") {
		AutoDisplayGraph = true;
		AutoDisplayTree = false;
		AutoDisplaySpreadSheet = false;
		$("#spreadSheetDiv").css("visibility", "hidden");
		$("#graphContainerDiv").css("visibility", "visible");
	} else if (mode == "TREE") {
		AutoDisplayGraph = false;
		AutoDisplayTree = true;
		AutoDisplaySpreadSheet = false;
		$("#spreadSheetDiv").css("visibility", "hidden");
		$("#graphContainerDiv").css("visibility", "visible");

	} else if (mode == "SPREADSHEET") {
		AutoDisplayGraph = false;
		AutoDisplayTree = false;
		AutoDisplaySpreadSheet = true;
		$("#spreadSheetDiv").css("visibility", "visible");
		$("#graphContainerDiv").css("visibility", "hidden");

	}

}


function setTraversalOutputMode(select) {
	var mode = $(select).val();
	if (mode == "GRAPH") {
		traversalToGraph = true;
		traversalToTree = false;
		traversalToSpredsheet = false;
	} else if (mode == "TREE") {
		traversalToGraph = false;
		traversalToTree = true;
		traversalToSpredsheet = false;

	} else if (mode == "SPEADSHEET") {
		traversalToGraph = false;
		traversalToTree = false;
		traversalToSpredsheet = true;

	}

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
		rIndices++;
		var p = query.indexOf(" return");
		var returnStr = query.substring(p);
		query = query.substring(0, p) + "-[r" + rIndices + "]-(" + variable + ":" + label + ")" + returnStr + "," + variable;
		$("#queryTA").val(query);
	}
	var labelPosition = query.indexOf(":" + label) + label.length + 1;
	labelsPositions[variable] = labelPosition;
	initialQuery = query;
	$(select).val("");

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
		query = query.substring(0, p) + "{id:" + node.id + "}" + query.substring(p);
	}
	$("#queryTA").val(query);
	executeQuery(QUERY_TYPE_MATCH, query, toGraph);
}

// function nodesLabelsSelect" size="7" onchange="onNodesLabelsSelect()">

function executeQuery(queryType, str, successFunction) {
	str += " limit " +"500"; $("#limit").val();
	nameLength =3//0; parseInt($("#labelLength").val());
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

	} else if (queryType == QUERY_TYPE_LABELS) {
		paramsObj = {
			mode : "GET",
			urlSuffix : "db/data/labels",

		}

	} else if (queryType == QUERY_TYPE_TRAVERSAL) {

		var payload = {
			"order" : "breadth_first",
			"max_depth" : parseInt($("#traversalDepth").val()),
			"uniqueness" : "relationship_global",
			"return_filter" : {
				// "body" : "position.length() > 3",//
				// "body" :
				// "position.endNode().getProperty('name').toLowerCase().contains('t')",//
				"body" : $("#traversalFilter").val(),
				"language" : "javascript"
			}
		}

		// console.log(JSON.stringify(payload));

		paramsObj = {
			mode : "POST",
			urlSuffix : "db/data/node/" + str + "/traverse/path",
			payload : JSON.stringify(payload)
		}

	} else {
		setMessage("noQuiery type specified", red);
		return;
	}

	if (!params)
		params = "";

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
					str += errors[i].code + " : " + errors[i].message + "<br>" + JSON.stringify(paramsObj);
				}
				setMessage(str, red);
				return;
			}

			if ($.isArray(data)) {// labels...
				if (successFunction)
					successFunction(data);
				else
					return data;
			}

			var results = data.results;
				
			if (results && results.length > 0) {// match..
				completeResult(results);
				if (successFunction) {
					successFunction(results);
				} else if (true) { // AutoDisplaySpreadSheet ||
									// AutoDisplaySpreadSheet) {
					currentVariables = results[0].columns;
					if (AutoDisplayGraph) {
						toGraph(results);

					}
					else if (AutoDisplayTree) {
						toTree(results);
					}
					else if (AutoDisplaySpreadSheet) {
						toSpreadsheet(results);
					}
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

function toTree(neoResult) {

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
				_id : obj.id,
				name : obj.name,
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
					_id : childObj.id,
					name : childObj.name,
					type : childObj.type,
					children : []
				})
			}
		}
	}

	var root = {
		id : -1,
		isRoot : true,
		name : "-" + currentVariable,
		children : []
	}
	for ( var key in distinctParentNodes) {
		root.children.push(distinctParentNodes[key]);
	}

	var d3tree = new D3Tree2($("#graphDiv"));
	d3tree.drawTree(root);
}

function completeResult(neoResult){
	var data = neoResult[0].data;
	for (var i = 0; i < data.length; i++) {
	
		var row = data[i].row;
		for (var j= 0; j < 3; j++) {
		if(!row[j].id)
			row[j].id=row[j].nom;
		if(!row[j].name)
			row[j].name=row[j].nom;
		
		}
		if(!row[0].type)
			row[0].type=row[3][0];
		if(!row[1].type)
			row[1].type=row[4][0];
	}
}

function toGraph(neoResult) {
	// Compute the distinct nodes from the links.

	var xxx = neoResult[0];
	var data = neoResult[0].data;
	var variables = neoResult[0].columns;

	if (variables.length < 2) {
		setMessage("Graph needs at least 2 node types", "red");
		return;
	}

	var nodes = {};
	var links = [];
	var index = 0;
	setMessage(data.length + " rows retrieved", green);

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
	graph.cleanGraph();
	if (true || nodes.length > 0) {
		graph.nodeColors = nodeColors;
		graph.drawGraph();
	}

}

function onTraversalGo() {

	executeQuery(QUERY_TYPE_TRAVERSAL, selectedObject.neoId, function(neoResult) {
		var rootNode;
		var traversalRows = [];
		var distinctRows = [];
		var neoIndices = [];
		var p = 0;

		// tri sur les relations les plus longues
		neoResult.sort(function(a, b) {
			var la = a.nodes.length;
			var lb = b.nodes.length;
			if (la > lb)
				return 1;
			if (la < lb)
				return -1;
			return 0;

		});

		// recherche des objets à partir de id Neo

		for (var i = 0; i < neoResult.length; i++) {
			var relations = neoResult[i].nodes;

			var p = relations[0].lastIndexOf("/") + 1;

			for (var j = 1; j < relations.length; j++) {

				var obj = {
					startNeoId : parseInt(relations[j - 1].substring(p)),
					endNeoId : parseInt(relations[j].substring(p))
				}

				// on ne stocke les relations qu'une seule fois
				var str = obj.startNeoId + "-" + obj.endNeoId;
				if (distinctRows.indexOf(str) < 0) {
					distinctRows.push(str);
					traversalRows.push(obj);
				}
				if (neoIndices.indexOf(obj.startNeoId) < 0)
					neoIndices.push(obj.startNeoId)
				if (neoIndices.indexOf(obj.endNeoId) < 0)
					neoIndices.push(obj.endNeoId)

			}
			/*
			 * if (neoIndices.length > 10) break;
			 */
		}
		var str = JSON.stringify(neoIndices);

		executeQuery(QUERY_TYPE_MATCH, " Match (n) where id(n) in " + str + " return n,id(n),labels(n)", function(neoResult) {

			var nodes = {};

			for (var i = 0; i < neoResult[0].data.length; i++) {

				var node = neoResult[0].data[i];
				if (!rootNode && node.row[0].id == selectedObject.id) {
					rootNode = node.row[0];
				}
				node.row[0].label = node.row[2][0];
				node.row[0].type = node.row[2][0];

				nodes[node.row[1]] = node.row[0];
				// /console.log(JSON.stringify(nodes[node.row[1]]))
			}

			// creation des objets pour le graphe ou le tree
			data = [];
			var columns = [];
			for (var i = 0; i < traversalRows.length; i++) {

				var objStart = nodes[traversalRows[i].startNeoId];
				var objEnd = nodes[traversalRows[i].endNeoId];

				if (objStart && objEnd) {

					/*
					 * if(objEnd.label!="BU") continue;
					 */
					if (i == 0) {
						columns.push(objStart.label);
						columns.push(objEnd.label);
					}
					var row = [];
					row.push(objStart);
					row.push(objEnd);
					data.push({
						row : row
					});
					// console.log(JSON.stringify(row));
				}
			}

			var output = [ {
				columns : columns,
				data : data
			} ];

			var relations = output[0].data;

			if (traversalToSpredsheet) {
				var dataS = flattenRelations(relations);
				if (dataS.length == 0)
					return;
				var spreadsheetHeaders = [];
				for ( var key in dataS[0]) {
					spreadsheetHeaders.push(key);
				}
				setMessage(dataS.length + " rows retrieved", green)
				$("#graphDiv").html();
				spreadsheet = new Spreadsheet("graphDiv");
				spreadsheet.headers = spreadsheetHeaders;
				spreadsheet.load(dataS);
			}
			if (traversalToGraph)
				toGraph(output);
			if (traversalToTree) {
				if (!rootNode)
					return;
				var treeRoot = {
					name : rootNode.name,
					label : rootNode.name,
					id_ : rootNode.id,
					type : rootNode.type
				}

				recursiveBuildTreeDone = [];
				recursiveBuildTree(relations, treeRoot)
				$("#graphDiv").html("");
				d3tree = new D3Tree2($("#graphDiv"));
				d3tree.drawTree(treeRoot);

			}

		});

	});
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
			var occurences = recursiveBuildTreeOccurences[obj.type + "_" + obj.id];
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

function toSpreadsheet(neoResult) {

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
	executeQuery(QUERY_TYPE_MATCH, " Match (n:" + selectedObject.type + "{id:" + selectedObject.id + "}) return id(n)", function(result) {
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

function autocompleteWord(select) {
	wordsSelect.options.length = 0;
	var word=$(select).val();
	if (word && word.length > 2) {
		var str = "MATCH (n)   WHERE n.nom =~ '(?i).*" + word + ".*' RETURN n,id(n),labels(n)";
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
						str += errors[i].code + " : " + errors[i].message + "<br>" + JSON.stringify(paramsObj);
					}
					setMessage(str, red);
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
	}
	;

}

function fillWordsSelect(neoResult){
	var xxx = neoResult[0];
	var data = neoResult[0].data;
	var variables = neoResult[0].columns;

	

	var nodes = {};
	var links = [];
	var index = 0;
	setMessage(data.length + " rows retrieved", green);
 var outData=[{name:"",id:-1}];
	for (var i = 0; i < data.length; i++) {
		var row = data[i].row;
		var obj={
				name: "["+row[2][0]+"] "+row[0].nom,
				id:row[1],
				label:row[2][0],
				color:nodeColors[row[2][0]]
				
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
			value : item[valueField]
		}));
	});
	var str=$(select).attr("id");
	$.each(data, function(i, item) {
		if(i>0 && item.color)
			$("#"+str+" option:eq("+i+")").css("color",item.color);
		})
	}




function onWordSelect(select){
	var id=$(select).val();
	if(id==-1)
		return;
//	var query="MATCH (n)-[r]-(m) WHERE ID(n) ="+id+" OR  ID(m)  ="+id+" RETURN n,m ";
	var query="MATCH (n)-[r]-(m) WHERE ID(n) ="+id+" RETURN n,m,r,labels(n),labels(m) ";
	var result=executeQuery(QUERY_TYPE_MATCH, query, null);
	if (result==-1){
		query="MATCH (n)-[r]-(m) WHERE ID(m) ="+id+" RETURN n,m ,r,labels(n),labels(m) ";
		result=executeQuery(QUERY_TYPE_MATCH, query, null);
	}
		
	
}
