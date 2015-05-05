/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.image;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.htmlhifive.testlib.core.AssertImage;
import com.htmlhifive.testlib.image.model.CompareOption;

public class ImageUtility {
	/**
	 * 2つの画像を比較し、一致しない領域の座標リストを返す。 TODO Optionは動作しない
	 *
	 * @param img1 比較画像1
	 * @param img1Area 比較画像1の比較エリア
	 * @param img2 比較画像2
	 * @param img2Area 比較画像2の比較エリア
	 * @param options オプション
	 * @param filePath ファイルパス
	 * @return
	 */
	public static List<Point> compareImages(BufferedImage img1, Rectangle img1Area, BufferedImage img2,
			Rectangle img2Area, CompareOption[] options, String filePath) {
		// TODO: Optionを実装する

		if (img1Area == null) {
			img1Area = new Rectangle(0, 0, img1.getWidth(), img1.getHeight());
		}
		if (img2Area == null) {
			img2Area = new Rectangle(0, 0, img2.getWidth(), img2.getHeight());
		}

		BufferedImage expectedSubImage = img1.getSubimage((int) img1Area.getX(), (int) img1Area.getY(),
				(int) img1Area.getWidth(), (int) img1Area.getHeight());
		BufferedImage actualSubImage = img2.getSubimage((int) img2Area.getX(), (int) img2Area.getY(),
				(int) img2Area.getWidth(), (int) img2Area.getHeight());

		// 共通部分以外は、diff設定する

		Integer startDiffX = null;
		Integer endDiffX = null;
		Integer startDiffY = null;
		Integer endDiffY = null;

		if (expectedSubImage.getWidth() > actualSubImage.getWidth()) {
			startDiffX = (int) img1Area.getX() + actualSubImage.getWidth() + 1;
			endDiffX = (int) img1Area.getX() + expectedSubImage.getWidth();
		} else if (expectedSubImage.getWidth() < actualSubImage.getWidth()) {
			startDiffX = (int) img1Area.getX() + expectedSubImage.getWidth() + 1;
			endDiffX = (int) img1Area.getX() + actualSubImage.getWidth();
		} else {
			startDiffX = (int) img1Area.getX() + expectedSubImage.getWidth();
			endDiffX = (int) img1Area.getX() + expectedSubImage.getWidth();
		}

		if (expectedSubImage.getHeight() > actualSubImage.getHeight()) {
			startDiffY = (int) img1Area.getY() + actualSubImage.getHeight() + 1;
			endDiffY = (int) img1Area.getY() + expectedSubImage.getHeight();
		} else if (expectedSubImage.getHeight() < actualSubImage.getHeight()) {
			startDiffY = (int) img1Area.getY() + expectedSubImage.getHeight() + 1;
			endDiffY = (int) img1Area.getY() + actualSubImage.getHeight();
		} else {
			startDiffY = (int) img1Area.getY() + expectedSubImage.getHeight();
			endDiffY = (int) img1Area.getY() + expectedSubImage.getHeight();
		}

		List<Point> diffPoints = new ArrayList<Point>();
		for (int i = startDiffX; i < endDiffX; i++) {
			for (int j = 0; j < endDiffY; j++) {
				Point diffPoint = new Point(i, j);
				diffPoints.add(diffPoint);
			}
		}

		for (int i = startDiffY; i < endDiffY; i++) {
			for (int j = 0; j < startDiffX; j++) {
				Point diffPoint = new Point(j, i);
				diffPoints.add(diffPoint);
			}
		}

		// 共通部分のdiffを取得する
		int width = img1.getWidth() < img2.getWidth() ? img1.getWidth() : img2.getWidth();
		int height = img1.getHeight() < img2.getHeight() ? img1.getHeight() : img2.getHeight();

		int[] expectedArr = expectedSubImage.getRGB((int) img1Area.getX(), (int) img1Area.getY(), width, height, null,
				0, width);
		int[] actualArr = actualSubImage.getRGB((int) img2Area.getY(), (int) img2Area.getY(), width, height, null, 0,
				width);

		for (int i = 0; i < expectedArr.length; i++) {
			if (expectedArr[i] != actualArr[i]) {
				int x = (i % width) + (int) img1Area.getX();
				int y = (i / width) + (int) img1Area.getY();

				Point diffPoint = new Point(x, y);
				diffPoints.add(diffPoint);
			}
		}

		return diffPoints;
	}

	/**
	 * 画像をdeepコピーする。subImageで来ることがあるためrasterも範囲を指定する。
	 *
	 * @param image
	 * @return
	 */
	private static BufferedImage getDeepCopyImage(BufferedImage image) {
		ColorModel cm = image.getColorModel();
		boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
		WritableRaster raster = image.copyData(null);
		WritableRaster rasterChild = (WritableRaster) raster.createChild(0, 0, image.getWidth(), image.getHeight(),
				image.getMinX(), image.getMinY(), null);
		return new BufferedImage(cm, rasterChild, isAlphaPremultiplied, null);
	}

	/**
	 * @param img
	 * @param diffPoints
	 * @return
	 */
	public static BufferedImage getMarkedImage(BufferedImage img, List<Point> diffPoints) {
		List<Rectangle> diffRectangles = convertDiffPointsToAreas(diffPoints);
		BufferedImage markedImg = null;
		try {
			// 異なるピクセルの左上にマーカーを置いていく
			BufferedImage mark = null;
			mark = ImageIO.read(AssertImage.class.getClassLoader().getResource("mark.png"));

			// マーカーの方が範囲が大きい場合、その範囲の画像を作成する
			int markerMaxX = img.getMinX() + img.getWidth();
			int markerMaxY = img.getMinY() + img.getHeight();
			int markerMinX = img.getMinX();
			int markerMinY = img.getMinY();

			boolean isExtend = false;

			for (Rectangle rectangle : diffRectangles) {
				// マーカーの最大値を取得する

				if (markerMaxX < (int) rectangle.getMaxX() + 4) {
					markerMaxX = (int) rectangle.getMaxX() + 4;
					isExtend = true;
				}
				if (markerMaxY < (int) rectangle.getMaxY() + 4) {
					markerMaxY = (int) rectangle.getMaxY() + 4;
					isExtend = true;
				}

				if (markerMinX > rectangle.getMinX() - 30) {
					markerMinX = (int) rectangle.getMinX() - 30;
					isExtend = true;
				}

				if (markerMinY > rectangle.getMinY() - 25) {
					markerMinY = (int) rectangle.getMinY() - 25;
					isExtend = true;
				}
			}
			Graphics marker;

			if (isExtend) {
				//画像が、マーカーの範囲を超えている場合コピーする
				markedImg = new BufferedImage(markerMaxX - markerMinX, markerMaxY - markerMinY,
						BufferedImage.TYPE_INT_ARGB);
				marker = markedImg.getGraphics();
				((Graphics2D) marker).setBackground(Color.GRAY);
				((Graphics2D) marker).clearRect(0, 0, markerMaxX - markerMinX, markerMaxY - markerMinY);
				marker.drawImage(img, -markerMinX, -markerMinY, img.getWidth(), img.getHeight(), null);
			} else {
				markedImg = getDeepCopyImage(img);
				marker = markedImg.getGraphics();
			}

			for (Rectangle markerGroup : diffRectangles) {

				marker.drawImage(mark, markerGroup.x - 30 - markerMinX, markerGroup.y - 25 - markerMinY, null);
				Graphics2D marker2 = (Graphics2D) marker;
				marker2.setColor(new Color(1.0f, 0.0f, 0.0f, 0.5f));
				marker2.setStroke(new BasicStroke(4.0f));

				marker2.drawRect(markerGroup.x - 2 - markerMinX, markerGroup.y - 2 - markerMinY, markerGroup.width + 4,
						markerGroup.height + 4);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return markedImg;
	}

	/**
	 * diff座標から近似の四角形を作成する。
	 *
	 * @param diffPoints
	 * @return
	 */
	private static List<Rectangle> convertDiffPointsToAreas(List<Point> diffPoints) {
		int margeFlag = 0;
		ArrayList<MarkerGroup> diffGroups = new ArrayList<MarkerGroup>();

		for (Point point : diffPoints) {
			MarkerGroup markerGroup = new MarkerGroup(new Point(point.x, point.y));
			for (MarkerGroup diffGroup : diffGroups) {
				if (diffGroup.isMarge(markerGroup)) {
					diffGroup.union(markerGroup);
					margeFlag = 1;
					break;
				}
			}
			if (margeFlag != 1) {
				diffGroups.add(markerGroup);
			}
			margeFlag = 0;
		}

		// 結合が無くなるまでループする
		int num = -1;
		while (num != 0) {
			num = 0;
			for (MarkerGroup rectangleGroup : diffGroups) {
				ArrayList<MarkerGroup> removeList = new ArrayList<MarkerGroup>();
				for (MarkerGroup rectangleGroup2 : diffGroups) {
					if (!rectangleGroup.equals(rectangleGroup2) && rectangleGroup.isMarge(rectangleGroup2)) {
						rectangleGroup.union(rectangleGroup2);
						// マージが発生した場合はカウントする
						num++;
						// マージしたモデルを削除対象として記録
						removeList.add(rectangleGroup2);
					}
				}
				if (num > 0) {
					// 削除対象がある場合は、リストから取り除く
					for (MarkerGroup removeModel : removeList) {
						diffGroups.remove(removeModel);
					}
					break;
				}
			}
		}

		// diffGroupsからRectangleのリストを作成
		List<Rectangle> rectangles = new ArrayList<Rectangle>();

		for (MarkerGroup markerGroup : diffGroups) {
			rectangles.add(markerGroup.getRectangle());
		}
		return rectangles;
	}
}
