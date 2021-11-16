// display image
	function previewImage() {
		var file = document.getElementById("fileImage").files;
		if (file.length > 0) {
 			var fileReader = new FileReader();
			fileReader.onload = function(event) {
			document.getElementById("thumbnail").setAttribute("src", event.target.result);		
			};
			fileReader.readAsDataURL(file[0]);		
		}
	}
	
