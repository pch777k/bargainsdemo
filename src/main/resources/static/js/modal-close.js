
$(document).ready(function() {
	$('.closeBtn').on('click', function(event) {
		event.preventDefault();
		var href = $(this).attr('href');
		$('#closeModal #closeRef').attr('href', href);
		$('#closeModal').modal();
	});
});