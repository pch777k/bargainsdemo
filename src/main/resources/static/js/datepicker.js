	$('.start-bargain').datepicker({
		autoclose: true,
	    todayHighlight: true,
			format: "dd-mm-yyyy",   		
	}).on('changeDate',function(e){
		$('.end-bargain').datepicker('setStartDate',e.date)
	});
	
	$('.end-bargain').datepicker({
		autoclose: true,
	    todayHighlight: true,
			format: "dd-mm-yyyy"
	}).on('changeDate',function(e){
		$('.start-bargain').datepicker('setEndDate',e.date)
	});
	
	$('.start-bargain, .end-bargain').datepicker('setStartDate', new Date());