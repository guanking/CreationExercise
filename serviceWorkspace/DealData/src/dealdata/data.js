/**
 * 
 */
function checkForm1() {
	var date = document.getElementById('startdate').value;
	var stopDate = document.getElementById('enddate').value;
	if (date != "" && stopDate != "") {
		date = date.replace(/-/g, '/');
		stopDate = stopDate.replace(/-/g, '/');
		var d1 = new Date(date);
		var d2 = new Date(stopDate);
		if (Date.parse(d1) - Date.parse(d2) > 0) {
			return false;
		}
	}
	if (date != "" && stopDate == "") {
		alert('��û���������ʱ�䣬���������룡');
		return false;
	}
	if (date == "" && stopDate != "") {
		alert('��û�����뿪ʼʱ�䣬���������룡');
		return false;
	}
	return true;
}