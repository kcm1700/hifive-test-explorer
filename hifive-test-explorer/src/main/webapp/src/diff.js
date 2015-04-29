/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
(function($) {
	/**
	 * This class is a &qout;logic&quot; for the test result comparison page.
	 *
	 * @class
	 * @memberOf hifive.test.explorer.logic
	 * @name TestResultDiffLogic
	 */
	var testResultDiffLogic = {
		/**
		 * @memberOf hifive.test.explorer.logic.TestResultDiffLogic
		 */
		__name: 'hifive.test.explorer.logic.TestResultDiffLogic',

		/**
		 *
		 * Get details of the screenshot.
		 * @memberOf hifive.test.explorer.logic.TestResultDiffLogic
		 * @param {string} id the id of the screenshot
		 * @returns {JqXHRWrapper}
		 */
		getScreenshot: function(id) {
			return h5.ajax({
				type: 'get',
				url: 'api/getScreenshot',
				data: {
					id: id
				}
			});
		}
	};

	h5.core.expose(testResultDiffLogic);
})(jQuery);
(function($) {
	/**
	 * This class is a controller for the test result comparison page.
	 *
	 * @class
	 * @memberOf hifive.test.explorer.controller
	 * @name TestResultDiffController
	 */
	var testResultDiffController = {
		/**
		 * @memberOf hifive.test.explorer.controller.TestResultDiffController
		 */
		__name: 'hifive.test.explorer.controller.TestResultDiffController',

		/**
		 * The &quot;Logic&quot; class
		 *
		 * @type Logic
		 * @memberOf hifive.test.explorer.controller.TestResultDiffController
		 */
		_testResultDiffLogic: hifive.test.explorer.logic.TestResultDiffLogic,

		/**
		 * Called after the controller has been initialized.<br>
		 * Get the id of the right screenshot, and update views.
		 *
		 * @memberOf hifive.test.explorer.controller.TestResultDiffController
		 */
		__ready: function() {
			// Get the id of the test result from url query parameters.
			var queryParams = hifive.test.explorer.utils.getParameters();
			if (!queryParams.hasOwnProperty('id')) {
				alert('ID not found');
				return;
			}

			var id = queryParams.id;

			// Get screenshot details
			this._testResultDiffLogic.getScreenshot(id).done(this.own(function(screenshot) {
				// Expected mode
				if (screenshot.expectedScreenshot == null) {
					this._hideActualMode();
					this._initDiffViews(id, id);
				} else {
					this._hideExpectedMode();
					var expectedId = screenshot.expectedScreenshot.id;
					this._initDiffViews(expectedId, id);
					this._initEdgeOverlapping(expectedId, id);
				}
			}));

			this._initializeSwipeHandle();
			this._initializeOnionHandle();
		},

		'input[name=flip-image] change': function() {
			var imageToBeShown = this.$find('input[name=flip-image]:checked').val();
			var $actual = this.$find('#quick-flipping .actual');
			if (imageToBeShown === 'actual')
				$actual.show();
			else
				$actual.hide();
		},

		/**
		 * @memberOf hifive.test.explorer.controller.TestResultDiffController
		 * @param {Number} expectedId ID of expected image
		 * @param {Number} actualId ID of actual image
		 */
		_initDiffViews: function(expectedId, actualId) {
			var format = hifive.test.explorer.utils.formatUrl;

			var actual = format('image/get', { id: actualId });
			var expected = format('image/get', { id: expectedId });
			var marker = format('image/getMarker', { expectedId: expectedId, actualId: actualId });

			$('.actual img.image').attr('src', actual);
			$('.expected img.image').attr('src', expected);
			$('img.marker').attr('src', marker);
		},

		/**
		 * @memberOf hifive.test.explorer.controller.TestResultDiffController
		 * @param {Number} expectedId ID of expected image
		 * @param {Number} actualId ID of actual image
		 */
		_initEdgeOverlapping: function(expectedId, actualId) {
			// Initialize <canvas>
			var expected = new Image(), actual = new Image();

			var d1 = $.Deferred(), d2 = $.Deferred();
			expected.onload = d1.resolve;
			actual.onload = d2.resolve;

			var format = hifive.test.explorer.utils.formatUrl;
			expected.src = format('image/getEdge', { id: expectedId, colorIndex: 1 });
			actual.src = format('image/getEdge', { id: actualId, colorIndex: 0 });

			$.when.apply($, [d1.promise(), d2.promise()]).done(function() {
				var canvas = $('#edge-overlapping canvas')[0];
				var native_width  = canvas.width  = expected.width;
				var native_height = canvas.height = expected.height;

				var context = canvas.getContext('2d');
				context.globalCompositeOperation = 'multiply';
				if (context.globalCompositeOperation == 'multiply') {
					context.drawImage(expected, 0, 0);
					context.drawImage(actual, 0, 0);
					initImageMagnifier();
				} else {
					// IE workaround
					var actualBlack = new Image();
					actualBlack.onload = function() {
						context.drawImage(expected, 0, 0);
						context.globalCompositeOperation = 'source-atop';
						context.drawImage(actualBlack, 0, 0);
						context.globalCompositeOperation = 'destination-over';
						context.drawImage(actual, 0, 0);
						initImageMagnifier();
					}
					actualBlack.src = format('image/getEdge', { id: actualId, colorIndex: 2 });
				}

				function initImageMagnifier() {
					// Image magnifier
					$('.large').css('background-image', 'url('+canvas.toDataURL('image/png')+')');
					$('#edge-overlapping .image-overlay').mousemove(function(e) {
						var magnify_offset = $(this).offset();
						var mx = e.pageX - magnify_offset.left;
						var my = e.pageY - magnify_offset.top;

						if (mx < $(this).width() && my < $(this).height() && mx > 0 && my > 0) {
							$('.large').fadeIn(100);
						} else {
							$('.large').fadeOut(100);
						} if ($('.large').is(':visible')) {
							var rx = Math.round(mx/$('.small').width()*native_width - $('.large').width()/2)*-1;
							var ry = Math.round(my/$('.small').height()*native_height - $('.large').height()/2)*-1;

							$('.large').css({
								left: mx - $('.large').width()/2,
								top: my - $('.large').height()/2,
								backgroundPosition: rx + 'px ' + ry + 'px'
							});
						}
					});
				}
			});
		},

		_hideActualMode: function() {
			this.$find('#actual-mode').hide();
		},

		_hideExpectedMode: function() {
			this.$find('#expected-mode').hide();
		},

		_initializeSwipeHandle: function() {
			var min = 0,max = 1000,step = 1;

			var $handle = this.$find('#swipe-handle');
			var $actual = this.$find('#swipe .actual');
			var $actualImg = this.$find('#swipe .actual > img');

			$handle.attr('min', min);
			$handle.attr('max', max);
			$handle.attr('step', step);
			$handle.val(min);

			var inputHandler = function() {
				var val = $handle.val();
				var percentage = ((val - min) / (max - min) * 100);
				$actual.css('left', percentage + '%');
				$actualImg.css('margin-left', (-percentage) + '%');
			};
			$handle.on('input', inputHandler);
			$handle.on('change', inputHandler); // for IE
		},

		_initializeOnionHandle: function() {
			var min = 0,max = 1000,step = 1;

			var $handle = this.$find('#onion-handle');
			var $actual = this.$find('#onion-skin .actual');

			$handle.attr('min', min);
			$handle.attr('max', max);
			$handle.attr('step', step);
			$handle.val(max);

			var inputHandler = function() {
				var val = $handle.val();
				var ratio = (val - min) / (max - min);
				$actual.css('opacity', ratio);
			};
			$handle.on('input', inputHandler);
			$handle.on('change', inputHandler); // for IE
		},
	};

	h5.core.expose(testResultDiffController);
})(jQuery);
$(function() {
	h5.core.controller('body>div.container',
			hifive.test.explorer.controller.TestResultDiffController);
});
