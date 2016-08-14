



function setAttributesValue(attrObject) {

	
	
	for ( var key in attrObject) {
		var value = "";
		if (currentUseCase)
			var value = currentUseCase[key];
		if (!value)
			value = "";
		var type = attrObject[key].type;

		if (type && type == 'readOnly') {
			attrObject[key].value = "&nbsp;:&nbsp;<b>" + value + "</b>";
			continue;
		}
		if (!type || type == 'text') {
			var cols = attrObject[key].cols;
			var rows = attrObject[key].rows;
			var strCols = ""

			if (rows) {// textarea
				if (cols)
					strCols = " cols='" + cols + "' ";
				rows = " rows='" + rows + "' ";
				value = "<textArea class='objAttr' " + strCols + rows
						+ "id='attr_" + key + "' > " + value + "</textarea>";
			} else {
				if (cols)
					strCols = " size='" + cols + "' ";
				value = "<input class='objAttr' " + strCols + "id='attr_"
						+ key + "' value=' " + value + "'>";
			}
		} else if (type == 'select') {
			var str = "<select class='objAttr' id='attr_" + key + "'>"
			str += "<option  value=''></option>";
			var options = selectOptionValues[useCaseAttrs[key].list];
			for (var i = 0; i < options.length; i++) {

				var selected = "";
				if (value == options[i].value)
					selected = " selected ";

				str += "<option value=" + options[i].value + " " + selected
						+ " >" + options[i].text + "</option>";
			}

			str += "</select>";
			value = str;
		}
		attrObject[key].value = value;
	}

}

function drawAttributes() {
	var str = "<table>"
	for ( var key in useCaseAttrs) {
		strVal = useCaseAttrs[key].value;
		str += "<tr><td>" + key + "</td><td>" + strVal + "</td></tr>";
	}
	str += "</table>";
	$("#AttrsSpan").html(str);

}


function setModifiedValues(obj, classId) {
	var fields = $(classId);
	if (!obj)
		obj = {}
	for (var i = 0; i < fields.length; i++) {

		var fieldId = $(fields[i]).attr('id').substring(5);
		var fieldValue = $(fields[i]).val();
		if (!fieldValue || fieldValue.length == 0)
			continue;
		if (fieldValue == " ")
			continue;

		obj[fieldId] = fieldValue;

	}
	return obj;

}

function drawFieldInputs(obj) {
	$("#attrsDiv").html("");
	var str = "<br><div id='inputPanel'>";
	str += "<button onclick=saveNodeInput(true)>Creer noeud</button>&nbsp;";
	str += "<div id='inputFieldsNodes'>";
	str += "<hr><table id='inputFieldsNodesTable'>";

	for ( var key in obj) {
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
	str += "</div>"
	$("#attrsDiv").html(str);

}