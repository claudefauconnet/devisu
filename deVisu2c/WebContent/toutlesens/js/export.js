var limit = 500000;

function exportNodeJson(outputType, outputObj) {
	var sourceSubGraph = $("#sourceSubGraph").val();
	if (sourceSubGraph && sourceSubGraph.length > 0)
		sourceSubGraph = " where n.subGraph=\"" + sourceSubGraph + "\" ";
	else
		sourceSubGraph = "";
	var label = "";
	var label = prompt("export nodes with label ?")
	if (label && label.length > 0)
		label = ":" + label;

	var statement = "MATCH (n" + label + ") " + sourceSubGraph
			+ "return  n,labels(n),ID(n) limit " + limit;
	queryExport(statement, exportNodesCallback, outputType, outputObj)
}

function exportRelJson(outputType, outputObj) {
	var sourceSubGraph;// = $("#sourceSubGraph").val();
	if (sourceSubGraph && sourceSubGraph.length > 0)
		sourceSubGraph = " where n.subGraph=\"" + sourceSubGraph + "\" ";
	else
		sourceSubGraph = "";
	var label = "";
	var label = prompt("export nodes with label ?")
	if (label && label.length > 0)
		label = ":" + label;
	
	var where="";//" where ID(m)=7876 ";

	var statement = "MATCH (n"+ label +")-[r]->(m)" + sourceSubGraph+where
			+ " return  Id(n),type(r),ID(m) limit " + limit;
	queryExport(statement, exportRelsCallback, outputType, outputObj)
}

function queryExport(statement, callback, outputType, outputObj) {

	var payload = {
		"statements" : [ {
			"statement" : statement
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
			var resultArray = data.results[0].data;
			callback(resultArray, outputType, outputObj)

		},
		error : function(xhr, err, msg) {
			console.log(xhr);
			console.log(err);
			console.log(msg);
		}

	});
}
function exportNodesCallback(resultArray, outputType, outputObj) {
	var targetSubGraph = $("#targetSubGraph").val();
	var nodes = {};

	for (var i = 0; i < resultArray.length; i++) {
		var node = resultArray[i].row[0];
		var label = resultArray[i].row[1][0];
		var id = resultArray[i].row[2];
		if (!nodes[label])
			nodes[label] = [];
		node.id = id;
		if (targetSubGraph.length > 0 && !node.subGraph)
			node.subGraph = subGraph;
		nodes[label].push(node);
	}
	if (outputType == "cypher") {
		var strAll = "";
		var jsonArray = [];
		var label2 = null;
		var i = 0;
		for (key in nodes) {
			jsonArray = nodes[key];
			label2 = key;

			var fields = {};

			for (var k = 0; k < jsonArray.length; k++) {// pour chaque label

				for (field in jsonArray[0]) {
					fields[field] = "aa";
				}
				var strFields = "{"
				var i = 0;
				for (field in fields) {
					if (i++ > 0)
						strFields += ",";
					strFields += field + ":obj." + field;
				}
				strFields += "}";
			}
			var str = "//------------" + label2 +"("+ jsonArray.length+")-----------\n";
			str += "forEach(obj in      ";
			str += myStringify(jsonArray);
			str += " | create (n:" + label2 + strFields + ") )";
			strAll += "\n//--------------\n" + str + "\n//--------------\n";

		}
		outputObj(strAll, "nodes");
	}

}

function exportRelsCallback(resultArray, outputType, outputObj) {
	var targetSubGraph = $("#targetSubGraph").val();
	var rels = {};

	for (var i = 0; i < resultArray.length; i++) {

		var id1 = resultArray[i].row[0];
		var rel = resultArray[i].row[1];
		var id2 = resultArray[i].row[2];
		if (!rels[rel])
			rels[rel] = [];
		var obj = {
			id1 : id1,
			rel : rel,
			id2 : id2

		}
		if (targetSubGraph.length > 0 && !obj.subGraph)
			obj.subGraph = targetSubGraph;
		rels[rel].push(obj);
	}
	if (outputType == "cypher") {
		var strAll = "";
		for (key in rels) {

			var label2 = null;

			jsonArray = rels[key];
			label2 = key;

			var str = "\n//------------" + label2 +"("+ jsonArray.length+ "-----------\n";
			str += "  WITH   ";
			str += myStringify(jsonArray);
			// str += " AS objs UNWIND objs AS obj MATCH (node1),(node2) "

			// + "where id(node1)= obj.id1 and id(node2)= obj.id2"

			str += " AS objs UNWIND objs AS obj  MATCH (node1{id:obj.id1}),(node2{id:obj.id2}) "
					+ " CREATE (node1)-[r:" + label2 + "]->(node2)";
			strAll += "\n//--------------\n" + str + "\n//--------------\n";
		}

	}
	outputObj(strAll, "relations");
}

function showResult(str, type) {
	download(type + ".cypher", str);
	if (str.length > 50000) {
		// console.log(str);

		str = str.substring(0, 50000)
				+ "\n//*********** truncated***********************************";
	}
	$("#result").val(str);

}

function myStringify(objArray) {
	var str = "[";
	for (var i = 0; i < objArray.length; i++) {
		if (i > 0)
			str += ",";
		var j = 0;
		str += "{";
		for ( var key in objArray[i]) {
			if (j++ > 0)
				str += ",";

			var val = objArray[i][key];
			if ($.isNumeric(val))
				str += key + ":" + val;
			else
				str += key + ":\"" + val + "\"";

		}
		str += "}";

	}
	str += "]";
	return str;

}

function download(filename, text) {
	var element = document.createElement('a');
	element.setAttribute('href', 'data:text/plain;charset=utf-8,'
			+ encodeURIComponent(text));
	element.setAttribute('download', filename);

	element.style.display = 'none';
	document.body.appendChild(element);

	element.click();

	document.body.removeChild(element);
}
