$(document).ready(
		function() {
			$('[data-toggle="tooltip"]').tooltip();
			$('[data-toggle="tooltip"]').on(
					'click',
					function() {
						$(this).attr('data-original-title',
								'Coupon copied to clipboard').tooltip('show')
								.attr('data-original-title', 'Copy to clipboard');
					});
		});
function copyCouponClick(copyCouponBtnId) {
	var copyTextarea = document.querySelector('#couponInput' + copyCouponBtnId.substring(13));
	copyTextarea.select();
	document.execCommand("copy");
}