
$(document).ready(function() {
	$('.delBtn').on('click', function(event) {
		event.preventDefault();
		var href = $(this).attr('href');
		$('#removeModal #delRef').attr('href', href);
		$('#removeModal').modal();
	});
});