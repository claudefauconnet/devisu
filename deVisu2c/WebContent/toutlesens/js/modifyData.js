/*******************************************************************************
 * TOUTLESENS LICENCE************************
 * 
 * The MIT License (MIT)
 * 
 * Copyright (c) 2016 Claude Fauconnet claude.fauconnet@neuf.fr
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 ******************************************************************************/

var dataModel = {
	labels : {},
	relations : {}
}
var clipboardNode;
var proposedRelations = [];


function onCreateNodeButton(label, json) {
	var xxx = dataModel;
	var str = "<br>Ajouter un noeud de type : <select id='labelTypesSelect' onchange='drawFieldInputs(null,$(this).val())'><option></option>"
	for ( var key in dataModel.labels) {
		str += "<option>" + key + "</option>";
	}
	str += "</select>"
	str += "<button onclick=addLabelType()>+</button>";
	$("#inputPanel").css("visibility", "visible");
	$("#modifyControls").css("visibility", "hidden");
	$("#graphDiv").css("visibility", "visible");
	$("#graphDiv").html(str);
}

function onHideInputMode() {
	$("#inputPanel").css("visibility", "hidden");
	$("#modifyControls").css("visibility", "hidden");
	$("#graphDiv").html("");
}
function onShowInputMode() {
	var password = prompt("mot de passe pour modifier ?");
	if (!password || password.length == 0)
		return;
	var query = {
		mode : "auth",
		value : password
	}

	$.ajax({
				type : "POST",
				url : "neoapi",
				data : query,
				dataType : "json",
				success : function(data, textStatus, jqXHR) {

					if (!data || data.length == 0) {

						$("#modifyPalette").append("mauvais mot de passe");
						return;
					}
					if (data.auth == "OK") {

						var str = "";//'<button onclick="onHideInputMode()">Lect. </button>';
						str += '<button onclick="onCreateNodeButton()">+</button>';
						$("#modifyPalette").html(str);
						$("#modifyControls").css("visibility", "visible");
						$("#graphDiv").html("");
						if (currentObject)
							drawModifyInputs(currentObject);
					}
					else{
						$("#modifyPalette").append("mauvais mot de passe");
						return;
					}
				},
				error : function(err) {
					console.log(err);
					$("#modifyPalette").append("mauvais mot de passe");
					return;
				}
			});
}


 function onModifyNodeButton() {
	 drawModifyInputs(currentObject);
 }


function onCopyNodeButton() {
	clipboardNode = currentObject;
	$("#currentNodeSpan").html(clipboardNode.name);
	$("#linkNodeButton").prop('disabled', false);

	$("#graphDiv").html(
			"Selectionnez le noeud a relier a " + currentObject.name
					+ " puis cliquez sur lier...");
}

function onLinkNodeButton() {
	var candidateRelations = dataModel.relations[currentObject.type];
	proposedRelations = [];
	if (candidateRelations && currentObject) {
		for (var i = 0; i < candidateRelations.length; i++) {
			if (candidateRelations[i].label2 == currentObject.type)
				proposedRelations.push(candidateRelations[i]);
		}

	}
	drawRelationDialog(proposedRelations);

}
function onDeleteNodeButton() {
	if (confirm("Confirmation : detruire le noeud :" + currentObject.name
			+ " et toutes ses relations ?"))
		deleteNodeInNeo4j(currentObject);
	onHideInputMode();
}

function drawRelationDialog(proposedRelations) {
	$("#graphDiv").html("");
	var str = "<br><div id='inputPanel'>";
	// str += "<button onclick=$('#graphDiv').html('')>Effacer</button>";
	str += "<button onclick=saveNewRelation()>Creer relation</button><br>";
	str += "<font color='blue'>"
	str += clipboardNode.name + "--&nbsp;&nbsp;";
	str += "<select id='newRelationSelect'><option></option>";
	for (var i = 0; i < proposedRelations.length; i++) {
		if (proposedRelations[i].direction == "normal")
			str += "<option >" + proposedRelations[i].relType + ">>></option>";
		else
			str += "<option ><<<" + proposedRelations[i].relType + "</option>";
	}
	str += "</select>";
	str += "<button onclick=addRelationType()>+</button>";

	str += "&nbsp;&nbsp;--" + currentObject.name;
	str += "</font>";

	$("#graphDiv").css("visibility", "visible");
	$("#graphDiv").html(str);
	$("#inputPanel").css("visibility", "visible");
	str += "</div>"
};

function drawFieldInputs(obj, type) {
	$("#graphDiv").html("");
	var str = "<br><div id='inputPanel'>";
	str += "<button onclick=saveNodeInput(true)>Creer noeud</button>&nbsp;";
	// str += "<button
	// onclick='onDeleteNodeButton()'>Supprimer</button>&nbsp;&nbsp;";
	// str += "<button onclick=$('#graphDiv').html('')>Effacer</button>";
	str += "<div id='inputFieldsNodes'>";
	str += "<hr><table id='inputFieldsNodesTable'>";
	str += "<tr><td><font color='blue'>LABEL</font></td><td><input  class='inputFieldNode' id='field_LABEL' value="
			+ type + "></td></tr>";
	str += "<button onclick=addPropertyToNode('')>+</button>";
	for (var i = 0; i < dataModel.labels[type].length; i++) {
		var fieldName = dataModel.labels[type][i];
		var fieldValue = "";
		if (fieldName == "subGraph" && subGraph) {
			fieldValue = subGraph;
		}
		str += "<tr>"
		str += "<td>" + fieldName
				+ " : </td><td><input class='inputFieldNode' value='" 
				+ fieldValue + "' id='field_" + fieldName + "'>" + "<td>";
		str += "</tr>"
	}	

	str += "</table>"


	str += "</div>"
	str += "</div>"

	$("#graphDiv").append(str);
	$("#graphDiv").css("visibility", "visible");
	$("#inputFieldsNodes").css("overflow", "visible");
	$("#inputPanel").css("visibility", "visible");
}

function drawModifyInputs(obj) {
	$("#graphDiv").html("");
	var str = "<br><div id='inputPanel'>";
	str += "<button onclick=saveNodeInput()>Enregistrer</button>&nbsp;";
	str += "<button onclick='onDeleteNodeButton()'>Supprimer</button>&nbsp;&nbsp;";

	// str += "<button
	// onclick=$('#graphDiv').html('')>Effacer</button>&nbsp;<br>";

	str += "<div id='inputFieldsNodes'>";
	str += "<hr><table id='inputFieldsNodesTable'>";
	str += "<tr><td><font color='blue'>LABEL</font></td><td><input  class='inputFieldNode' id='field_LABEL' value="
			+ obj.type + "></td></tr>";

	if (subGraph && !obj.subGraph) {
		obj.subGraph = subGraph;
	}
	for ( var key in obj) {
		if (key == "id" || (key == "name" && obj.nom))
			continue;
		var fieldName = key;
		var fieldValue = obj[key];

		str += "<tr>"
		str += "<td>" + fieldName
				+ " : </td><td><input class='inputFieldNode' value='"
				+ fieldValue + "' id='field_" + fieldName + "'>" + "<td>";
		str += "</tr>"
	}

	str += "</table>"
	str += "<button onclick=addPropertyToNode('')>+</button>";

	str += "</div>"
	var html = $("#graphDiv").html();
	$("#graphDiv").html(html + str);
	$("#graphDiv").css("visibility", "visible");
	$("#inputPanel").css("visibility", "visible");
}

function addPropertyToNode() {
	var fieldName = prompt("nom de la nouvelle propriete");
	if (!fieldName || fieldName.length == 0)
		return;
	var str = "<td>" + fieldName
			+ " : </td><td><input class='inputFieldNode'  id='field_"
			+ fieldName + "'>" + "<td>";
	// var html = $("#inputFieldsNodesTable").html();
	$("#inputFieldsNodesTable").append(str);
}

function addRelationType() {
	var relType = prompt("nom de la nouvelle propriete");
	if (!relType || relType.length == 0)
		return;
	$("#newRelationSelect").append(
			$('<option selected="selected"></option>').val(relType).html(
					relType));

	var obj = {
		label1 : clipboardNode.type,
		relType : relType,
		label2 : currentObject.type,
		direction : "normal"
	};
	if (!dataModel.relations[clipboardNode.type])
		dataModel.relations[clipboardNode.type] = [];
	dataModel.relations[clipboardNode.type].push(obj);
	proposedRelations.push(obj);
	var obj = {
		label1 : currentObject.type,
		relType : relType,
		label2 : clipboardNode.type,
		direction : "inverse"
	};
	if (!dataModel.relations[currentObject.type])
		dataModel.relations[currentObject.type] = [];
	dataModel.relations[currentObject.type].push(obj);

}
function addLabelType() {
	var label = prompt("nom du nouveau label");
	if (!label || label.length == 0)
		return;
	$("#labelTypesSelect").append(
			$('<option selected="selected"></option>').val(label).html(label));
	dataModel.labels[label] = [ 'subGraph', 'nom' ];
	drawFieldInputs(null, label);
}
function saveNodeInput(isNew) {
	// var fields=$("#inputFieldsNodes").children();
	// var fields=$("[id^='field_']" ).children();
	// var fields=$("[id^='field_']" ).val();
	var fields = $(".inputFieldNode")
	// var fields=$(".inputFieldNode" ).val();
	var obj = {}
	for (var i = 0; i < fields.length; i++) {

		var fieldId = $(fields[i]).attr('id').substring(6);
		var fieldValue = $(fields[i]).val();
		if (!fieldValue || fieldValue.length == 0)
			continue;

		obj[fieldId] = fieldValue

		obj[fieldId] = fieldValue;
		
	}

	writeNodeInNeo4j(obj, isNew);
	$("#graphDiv").html("");

}

function saveNewRelation() {

	var relation = proposedRelations[newRelationSelect.selectedIndex - 1];
	writeRelationInNeo4j(clipboardNode, relation, currentObject);
	$("#graphDiv").html("");

}
function deleteNodeInNeo4j(obj) {
	var query = "MATCH (n) where ID(n)=" + obj.id + " detach delete n";
	executeQuery(QUERY_TYPE_MATCH, query, function() {
		setMessage("Noeud supprimé !", "green");
	});

}
function writeNodeInNeo4j(obj, isNew) {
	var label = "";
	var props = "{"
	var i = 0;
	for ( var key in obj) {
		if (key == "LABEL")
			label = obj[key];
		else {
			var value = obj[key];
			if (i++ > 0)
				props += ",";
			if ($.isNumeric(value))
				props += key + ':' + value;
			else {
				props += key + ':"' + value + '"';
			}

		}

	}

	props += "}"

	if (isNew) {
		var query = "CREATE (n:" + label + props + ") RETURN n.name,ID(n)"
		executeQuery(QUERY_TYPE_MATCH, query, function() {
			setMessage("Noeud crée !", "green");
		});
	} else {
		var query = "MATCH (n) where ID(n)=" + currentObject.id + " SET n = "
				+ props + " RETURN n";

		executeQuery(QUERY_TYPE_MATCH, query, function() {
			setMessage("Noeud modifié !", "green");
		});

	}
}

function writeRelationInNeo4j(node1, relation, node2) {
	var query = "MATCH (n),(m)" + "WHERE ID(n)=" + node1.id + " AND ID(m)="
			+ node2.id + " CREATE (n)-[r:" + relation.relType
			+ " { name : n.name + '<->' +m.name }]->(m)" + " RETURN r";
	executeQuery(QUERY_TYPE_MATCH, query, function() {
		setMessage("Relation crée !", "green");
	});
}

function updateProperties(id, json) {
	/*
	 * MATCH (n { name: 'Andres' }) SET n.surname = 'Taylor' RETURN n
	 */

}

function getDBModel() {
	var where = "";
	if (subGraph)
		where = " where n.subGraph='" + subGraph + "'";
	var query = "MATCH (n) " + where + " RETURN distinct labels(n), count(*) ";
	console.log(query);
	executeQuery(QUERY_TYPE_MATCH, query, getDBNodelLabelsCallback);
	query = "MATCH (n)-[r]->(m) " + where
			+ " RETURN distinct labels(n),type(r),labels(m)";
	console.log(query);
	executeQuery(QUERY_TYPE_MATCH, query, getRelationsModelCallback);
}

function getDBNodelLabelsCallback(result) {
	var data = result[0].data;
	var points = {};
	for (var i = 0; i < data.length; i++) {
		var label = data[i].row[0][0];
		if (!label)
			return;
		var query = "MATCH (n:" + label + ") RETURN n,labels(n)  limit 1";
		executeQuery(QUERY_TYPE_MATCH, query, getDBModelPopertiesCalllback);
	}

}

function getDBModelPopertiesCalllback(result) {
	var data = result[0].data;
	var points = {};
	for (var i = 0; i < data.length; i++) {
		var obj = data[i].row[0];
		var label = data[i].row[1][0];
		if (!dataModel.labels[label])
			dataModel.labels[label] = [];
		for ( var key in obj)
			dataModel.labels[label].push(key);
	}
}





function getRelationsModelCallback(result) {
	var data = result[0].data;
	var points = {};
	for (var i = 0; i < data.length; i++) {
		var obj = {
			label1 : data[i].row[0][0],
			relType : data[i].row[1],
			label2 : data[i].row[2][0],
			direction : "normal"
		}
		if (!dataModel.relations[obj.label1])
			dataModel.relations[obj.label1] = [];

		dataModel.relations[obj.label1].push(obj);

	}

	for (var i = 0; i < data.length; i++) {
		var obj = {
			label1 : data[i].row[0][0],
			relType : data[i].row[1],
			label2 : data[i].row[2][0],
			direction : "inverse"
		}
		if (!dataModel.relations[obj.label2])
			dataModel.relations[obj.label2] = [];

		dataModel.relations[obj.label2].push(obj);

	}

}

function getProperties2(){
	
	var query={mode:"listProperties"}
	$.ajax({
		type : "POST",
		url : "neoapi",
		data : query,
		dataType : "json",
		success : function(data, textStatus, jqXHR) {

		var data_=data;
		},
		error : function(err) {
			console.log(err);
		//	$("#modifyPalette").append("error");
			return;
		}
	});
}
