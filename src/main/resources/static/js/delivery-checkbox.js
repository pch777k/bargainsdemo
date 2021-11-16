    // delivery checkbox
    var deliveryCheckbox = document.getElementById('free-delivery');
	var delivery = document.getElementById('delivery');
	function updateDelivery(){
		if(deliveryCheckbox.checked){
			delivery.value = 0;
			delivery.readOnly = true;
		} else {
			delivery.value = null;
			delivery.readOnly = false;
		}
	}