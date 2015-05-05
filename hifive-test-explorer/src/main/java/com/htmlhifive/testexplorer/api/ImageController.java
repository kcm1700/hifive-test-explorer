/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.api;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.htmlhifive.testexplorer.entity.ConfigRepository;
import com.htmlhifive.testexplorer.entity.Screenshot;
import com.htmlhifive.testexplorer.entity.ScreenshotRepository;
import com.htmlhifive.testexplorer.image.EdgeDetector;
import com.htmlhifive.testexplorer.image.ImageUtility;

@Controller
@RequestMapping("/image")
public class ImageController {

	@Autowired
	private ConfigRepository configRepo;
	@Autowired
	private ScreenshotRepository screenshotRepo;

	@Autowired
	private HttpServletRequest request;

	@SuppressWarnings("unused")
	private static Logger log = LoggerFactory.getLogger(ImageController.class);

	/**
	 * Get the image from id.
	 *
	 * @param id screenshot id
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value = "/get", method = RequestMethod.GET)
	public void getImage(@RequestParam Integer id, HttpServletResponse response) {
		Screenshot screenshot = screenshotRepo.findOne(id);

		// Send PNG image
		try {
			sendFile(getFile(screenshot), response);
		} catch (IOException e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	/**
	 * Get edge detection result of an image.
	 *
	 * @param id id of screenshot to be processed by edge detector.
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value = "/getEdge", method = RequestMethod.GET)
	public void getEdgeImage(@RequestParam Integer id,
							 @RequestParam(defaultValue = "-1") int colorIndex, HttpServletResponse response)
	{
		Screenshot screenshot = screenshotRepo.findOne(id);

		try {
			EdgeDetector edgeDetector = new EdgeDetector(0.5);

			switch (colorIndex) {
			case 0:
				edgeDetector.setForegroundColor(new Color(255, 0, 0, 255));
				break;
			case 1:
				edgeDetector.setForegroundColor(new Color(0, 0, 255, 255));
				break;
			}

			BufferedImage edgeImage = edgeDetector.DetectEdge(ImageIO.read(getFile(screenshot)));
			sendImage(edgeImage, response);
		} catch (IOException e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	/**
	 * Get the diff marker of comparison result. If there is no difference,
	 * return 1px*1px sized transparent image.
	 *
	 * @param actualId comparison source image id
	 * @param expectedId comparison target image id
	 * @param response HttpServletResponse
	 */
	@RequestMapping(value = "/getMarker", method = RequestMethod.GET)
	public void getMarker(@RequestParam Integer actualId, @RequestParam Integer expectedId, HttpServletResponse response) {
		Screenshot actualImg = screenshotRepo.findOne(actualId);
		Screenshot expectedImg = screenshotRepo.findOne(expectedId);

		try {
			// Read both image and compare
			BufferedImage actual = ImageIO.read(getFile(actualImg));
			BufferedImage expected = ImageIO.read(getFile(expectedImg));

			List<Rectangle> diff = ImageUtility.compare(expected, actual);
			BufferedImage result;
			if (!diff.isEmpty()) {
				BufferedImage bkground = new BufferedImage(actual.getWidth(), actual.getHeight(), BufferedImage.TYPE_INT_ARGB);
				result = ImageUtility.getMarkedImage(bkground, diff);
			} else {
				result = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
			}
			sendImage(result, response);
		} catch (IOException e) {
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}

	private File getFile(Screenshot screenshot) throws FileNotFoundException {
		String path =
				configRepo.findOne(ConfigRepository.ABSOLUTE_PATH_KEY).getValue() +
				File.separatorChar +
				"images" +
				File.separatorChar +
				screenshot.getTestExecution().getTimeString() +
				File.separatorChar +
				screenshot.getTestClass() +
				File.separatorChar +
				screenshot.getFileName() + ".png";

		File file = new File(path);
		if (!file.exists() || !file.isFile()) { throw new FileNotFoundException(path + " Not Found."); }
		return file;
	}

	private void sendFile(File file, HttpServletResponse response) throws IOException {
		response.setContentType("image/png");
		IOUtils.copy(new FileInputStream(file), response.getOutputStream());
	}

	private void sendImage(BufferedImage image, HttpServletResponse response) throws IOException {
		response.setContentType("image/png");
		ImageIO.write(image, "png", response.getOutputStream());
	}
}
