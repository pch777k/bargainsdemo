
$(document).ready(function() {
	$('.openBtn').on('click', function(event) {
		event.preventDefault();
		var href = $(this).attr('href');
		$('#openModal #openRef').attr('href', href);
		$('#openModal').modal();
	});
});