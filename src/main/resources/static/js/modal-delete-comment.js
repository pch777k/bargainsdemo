
$(document).ready(function() {
	$('.deleteBtn').on('click', function(event) {
		event.preventDefault();
		var href = $(this).attr('href');
		$('#deleteModal #deleteRef').attr('href', href);
		$('#deleteModal').modal();
	});
});