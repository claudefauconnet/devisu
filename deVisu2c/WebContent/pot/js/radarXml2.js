function RadarXml(xmlFileName, type) {

	// map des radarRole->fieldName
	thisxmlDoc = "";
	this.Xml_enumerations = new Array();
	this.Xml_fieldsMapping = new Object();
	this.nonVisibleFields = [ "color", "previousState", "group",
			"permissionEdit", "visible", "enumIds", "action" ];
	this.xmlFileUrl;
	this.type = type;
	this.displayType ;
	this.infosPage ;
	this.collectionName = "AAA";

	this.radars = {};
	this.xmlFileName = xmlFileName;
	currentRadarType = type;

	this.initEnumerations = function(doc) {
		var enums = doc.getElementsByTagName("enum");
		for (var i = 0; i < enums.length; i++) {
			enums[i].setAttribute("enumId", i);
			var obj = new Object();
			obj.id = i;
			if (enums[i].parentNode.getAttribute("isFilter") == "true")
				obj.fieldName = enums[i].parentNode.getAttribute("name");
			else
				obj.fieldName = enums[i].parentNode.getAttribute("radarRole");
			obj.fieldRealName = enums[i].parentNode.getAttribute("name");
			obj.isMultiValued = enums[i].parentNode
					.getAttribute("isMultivalued");

			obj.label = enums[i].getAttribute("label");
			obj.color = enums[i].getAttribute("color");
			obj.symbol = enums[i].getAttribute("symbol");
			obj.shape = enums[i].getAttribute("shape");
			obj.size = enums[i].getAttribute("size");
			obj.type = enums[i].getAttribute("type");
			obj.checked = false;
			this.Xml_enumerations.push(obj);
		}
	}

	this.xmlFileUrl = dataPath + "/" + this.xmlFileName + ".xml";
	var doc = getXmlDoc(this.xmlFileUrl);
	if (!doc) {
		setMessage("no xml file for radar" + this.xmlFileName, "red")
		return;
	}

	var radarElements = doc.getElementsByTagName("radar");
	for (var i = 0; i < radarElements.length; i++) {

		var radarName = radarElements[i].getAttribute("name");
		if (!radarName || radarName != this.type) {
			continue;
		}

		this.displayType=radarElements[i].getAttribute("displayType");
		this.infosPage=radarElements[i].getAttribute("infosPage");
		if (document.getElementById("title"))
			document.getElementById("title").innerHTML = radarName;
		detailsPageURL = radarElements[i].getAttribute("detailsPageUrl");
		this.collectionName = radarElements[i].getAttribute("collectionName");
		this.initEnumerations(radarElements[i]);

		var fields = radarElements[i].getElementsByTagName("field");
		for (var j = 0; j < fields.length; j++) {
			var role = fields[j].getAttribute("radarRole");
			if (role && role.length > 0)
				this.Xml_fieldsMapping[role] = fields[j].getAttribute("name");
			else
				this.Xml_fieldsMapping[fields[j].getAttribute("name")] = fields[j]
						.getAttribute("name");

			if (role == "horizontalAxis" || role == "radialAxis")
				radarAxes.push(fields[j].getAttribute("name"));

		}
		this.radars[currentRadarType] = radarElements[i];
		this.xmlDoc = radarElements[i];
		// return radarElements[i];

	}

	this.getRadarXmlDoc = function(type) {

		var aRadar = this.radars[this.type];
		if (type)
			aRadar = this.radars[type];
		if (aRadar)
			return aRadar;

	}

	this.uploadString = function(txt) {
		if (window.DOMParser) {
			parser = new DOMParser();
			this.xmlDoc = parser.parseFromString(txt, "text/xml");
		} else // code for IE
		{
			this.xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
			this.xmlDoc.async = false;
			this.xmlDoc.upload(txt);
		}
		return this.xmlDoc;
	}

	this.getXmlExemple = function() {
		if (!this.xmlDoc)
			this.xmlDoc = this.getRadarXmlDoc(currentRadarType);
		var fields = this.xmlDoc.getElementsByTagName("field");

	}

	this.Xml_getImageUrl = function() {
		if (!this.xmlDoc)
			this.xmlDoc = this.getRadarXmlDoc(currentRadarType);
		var img = "";
		var radars = this.xmlDoc.getElementsByTagName("radar");
		for (var i = 0; i < radars.length; i++) {
			if (radars[i].getAttribute("name") == dbName) {
				img = radars[i].getAttribute("background-image");
				break;
			}
		}
		return img;

	}

	// !!!!! A REVOIR ne gï¿½re qu'un seul lien (pb filed name...
	this.XML_getUrlLinkPreffix = function(fieldName) {
		if (!this.xmlDoc)
			this.xmlDoc = this.getRadarXmlDoc(currentRadarType);
		// fieldName=this.Xml_fieldsMapping[fieldName];
		var str = "";
		var links = this.xmlDoc.getElementsByTagName("linkUrlPreffix");
		for (var i = 0; i < links.length; i++) {
			// ATTENTION pas fini (true)
			var parentNode = links[i].parentNode.getAttribute("name");
			if (true || parentNode.getAttribute("name").value == fieldName) {
				str = links[i].innerHTML;
				if (!str)
					str = links[i].text;
				break;
			}
		}
		return str;

	}

	this.XML_getIdFieldName = function() {
		if (!this.xmlDoc)
			this.xmlDoc = this.getRadarXmlDoc(currentRadarType);
		var str = "";
		var fields = this.xmlDoc.getElementsByTagName("fields");
		for (var i = 0; i < fields.length; i++) {
			if (fields[i].getAttribute("radarRole") == "id") {
				fields[i].getAttribute("name");
				break;
			}
		}
		return str;

	}

	this.XML_getFieldAttribute = function(fieldName, attr) {

		if (!this.xmlDoc)
			this.xmlDoc = this.getRadarXmlDoc(currentRadarType);
		this.Xml_fieldsMapping[fieldName];
		var str = "";
		var fields = this.xmlDoc.getElementsByTagName("field");
		for (var i = 0; i < fields.length; i++) {
			var val = fields[i].getAttribute("radarRole");
			if (val == fieldName) {
				str = fields[i].getAttribute(attr);
				break;
			}
		}
		return str;

	}

	this.XML_getFieldForRole = function(role) {

		if (!this.xmlDoc)
			this.xmlDoc = this.getRadarXmlDoc(currentRadarType);

		var fields = this.xmlDoc.getElementsByTagName("field");
		for (var i = 0; i < fields.length; i++) {
			var val = fields[i].getAttribute("radarRole");
			if (val == role) {
				return fields[i].getAttribute("name");

			}
		}

	}

	this.XML_getFieldRole = function(fieldName) {

		if (!this.xmlDoc)
			this.xmlDoc = this.getRadarXmlDoc(currentRadarType);

		var fields = this.xmlDoc.getElementsByTagName("field");
		for (var i = 0; i < fields.length; i++) {
			var val = fields[i].getAttribute("name");
			if (val == fieldName) {
				return fields[i].getAttribute("radarRole");

			}
		}

	}

	this.XML_getField = function(fieldName) {

		if (!this.xmlDoc)
			this.xmlDoc = this.getRadarXmlDoc(currentRadarType);

		var fields = this.xmlDoc.getElementsByTagName("field");
		for (var i = 0; i < fields.length; i++) {
			var val = fields[i].getAttribute("name");
			if (val == fieldName) {
				return fields[i];
			}
		}
	}

	this.XML_getFieldNames = function(_xmlDoc) {
		if (!_xmlDoc)
			_this.xmlDoc = this.xmlDoc;
		var fieldNames = [];
		var fields = _this.xmlDoc.getElementsByTagName("field");
		for (var i = 0; i < fields.length; i++) {
			fieldNames.push(fields[i].getAttribute("name"));
		}
		return fieldNames;
	}

	this.Xml_getEnumeration = function(id) {
		for (var i = 0; i < this.Xml_enumerations.length; i++) {
			if (this.Xml_enumerations[i].id == id) {
				return this.Xml_enumerations[i];
			}
		}
	}

	this.getFieldType = function(fieldName) {
		if (!this.xmlDoc)
			this.xmlDoc = this.getRadarXmlDoc(currentRadarType);
		var fields = this.xmlDoc.getElementsByTagName("field");
		for (var i = 0; i < fields.length; i++) {
			if (fields[i].getAttribute("name") == fieldName) {
				return fields[i].getAttribute("this.type");
			}
		}
		return "NO TYPE";
	}

	this.Xml_getEnumerationByValue = function(type, value) {
		for (var i = 0; i < this.Xml_enumerations.length; i++) {
			if (this.Xml_enumerations[i][this.type] == value) {
				return this.Xml_enumerations[i];
			}
		}
	}

	this.Xml_getRealValue = function(enumAttr, enumFieldName, val) {

		if (!this.xmlDoc)
			this.xmlDoc = this.getRadarXmlDoc(currentRadarType);
		var color = "#eef";
		var enums = this.xmlDoc.getElementsByTagName("enum");
		for (var i = 0; i < this.Xml_enumerations.length; i++) {
			// console.log(enumAttr+" "+ enumFieldName+" "+val+"
			// "+this.Xml_enumerations[i].fieldName);
			if (!enumFieldName || this.Xml_enumerations[i].fieldName == enumFieldName) {
				if (this.Xml_enumerations[i].label == val) {
					color = this.Xml_enumerations[i][enumAttr];
					break;
				}

			}
		}
		return color;
	}

	this.Xml_getLegendElements = function() {

		if (!this.xmlDoc)
			this.xmlDoc = this.getRadarXmlDoc(currentRadarType);
		var fields = this.xmlDoc.getElementsByTagName("enum");
		var legendElts = [];
		for (var i = 0; i < fields.length; i++) {
			var role = fields[i].parentNode.getAttribute("radarRole");
			var type = fields[i].parentNode.getAttribute("name");
			fields[i].setAttribute("type", type);
			if (role == 'symbol' || role == 'color' || role == 'shape'
					|| role == 'size')
				legendElts.push(fields[i]);
		}
		return legendElts;

	}
	this.Xml_getfilterNames = function() {
		if (!this.xmlDoc)
			this.xmlDoc = this.getRadarXmlDoc(currentRadarType);
		var filters = [];
		var fields = this.xmlDoc.getElementsByTagName("field");
		for (var i = 0; i < fields.length; i++) {
			var isFilter = fields[i].getAttribute("isFilter");
			if (isFilter != null)// && role=="true" )
				filters.push(fields[i].getAttribute("name"));

			var role = fields[i].parentNode.getAttribute("radarRole");
			if (role == 'radialAxis' || role == 'horizontalAxis')
				filters.push(fields[i].getAttribute("name"));
		}
		return filters;

	}

	this.Xml_onFieldChange = function(select) {

	}

}