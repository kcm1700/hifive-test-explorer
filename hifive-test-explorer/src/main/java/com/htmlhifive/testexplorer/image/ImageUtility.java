/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.image;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import com.htmlhifive.testlib.core.AssertImage;
import com.htmlhifive.testlib.image.model.CompareOption;

public class ImageUtility {

	public static double[][] calcIntegralImage(BufferedImage source) {
		double[][] integralImage = new double[source.getHeight()][source.getWidth()];
		Raster raster = source.getRaster();
		int[] pixel = new int[raster.getNumDataElements()];
		double leftNum, upNum, leftUpNum;
		for (int y = 0; y < source.getHeight(); y++) {
			for (int x = 0; x < source.getWidth(); x++) {
				leftNum = (x == 0) ? 0 : integralImage[y][x - 1];
				upNum = (y == 0) ? 0 : integralImage[y - 1][x];
				leftUpNum = (x == 0 || y == 0) ? 0 : integralImage[y - 1][x - 1];
				integralImage[y][x] = leftNum + upNum + raster.getPixel(x, y, pixel)[0] - leftUpNum;
			}
		}
		return integralImage;
	}

	/**
	 * @param contained
	 * @param container
	 * @return
	 */
	public static boolean isContained(BufferedImage contained, BufferedImage container) {
		// 元画像の積分画像を作成
		double[][] integralImage = ImageUtility.calcIntegralImage(container);

		double sumContent = 0;
		Raster r = contained.getRaster();
		int[] dArray = new int[r.getNumDataElements()];
		for (int x = 0; x < r.getWidth(); x++) {
			for (int y = 0; y < r.getHeight(); y++) {
				sumContent += r.getPixel(x, y, dArray)[0];
			}
		}

		int contentWidth = contained.getWidth();
		int contentHeight = contained.getHeight();
		double p0, p1, p2, p3, sumContainer;
		BufferedImage window;
		for (int y = 0; y < container.getHeight() - contained.getHeight() + 1; y++) {
			// System.out.println(y);
			for (int x = 0; x < container.getWidth() - contained.getWidth() + 1; x++) {
				p0 = integralImage[y + contentHeight - 1][x + contentWidth - 1];
				p1 = (x == 0) ? 0 : integralImage[y + contentHeight - 1][x - 1];
				p2 = (y == 0) ? 0 : integralImage[y - 1][x + contentWidth - 1];
				p3 = (x == 0 || y == 0) ? 0 : integralImage[y - 1][x - 1];
				sumContainer = p0 - p1 - p2 + p3;
				// System.out.println(sumContainer / numPixel);

				// System.out.println(sumContainer - sumContent);
				if (sumContainer == sumContent) {
					window = container.getSubimage(x, y, contentWidth, contentHeight);
					if (imgEquals(window, contained)) {
						System.out.println("(x, y): (" + x + ", " + y + ")");
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * BufferedImageが等しいかどうかの2値を返す
	 *
	 * @param img1
	 * @param img2
	 * @return
	 */
	private static boolean imgEquals(BufferedImage img1, BufferedImage img2) {
		if (img1.getWidth() != img2.getWidth() || img1.getHeight() != img2.getHeight()) {
			return false; // サイズ不一致
		} else {
			int width = img1.getWidth();
			int height = img1.getHeight();
			return Arrays.equals(img1.getRGB(0, 0, width, height, null, 0, width),
					img2.getRGB(0, 0, width, height, null, 0, width));
		}
	}

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
	 * 元画像をマスクしたdiff画像を生成する。
	 *
	 * @param base 元画像
	 * @param diffModel 比較で一致しなかったピクセルの集合
	 * @return diff画像のBufferedImage
	 */
	public static BufferedImage getMaskedImage(BufferedImage base, List<Rectangle> diffRectangles) {

		BufferedImage copyImage = getDeepCopyImage(base);

		Color color = Color.RED;
		Graphics grf = copyImage.getGraphics();
		grf.setColor(color);

		// rectsで指定される領域をマスクする
		for (Rectangle rect : diffRectangles) {
			Point loc = rect.getLocation();
			Dimension size = rect.getSize();
			grf.fillRect(loc.x, loc.y, size.width, size.height);
		}
		grf.dispose();

		return copyImage;

	}

	/**
	 * 画像をグレースケールにする see: http://d.hatena.ne.jp/kgu/20130324/1364111482
	 *
	 * @param image 画像
	 */
	public static BufferedImage getGrayScaleImage(BufferedImage image) {
		BufferedImage copyImage = getDeepCopyImage(image);
		WritableRaster raster = copyImage.getRaster();

		int[] pixelBuffer = new int[raster.getNumDataElements()];

		for (int y = 0; y < raster.getHeight(); y++) {
			for (int x = 0; x < raster.getWidth(); x++) {
				// ピクセルごとに処理

				raster.getPixel(x, y, pixelBuffer);

				// 単純平均法((R+G+B)/3)でグレースケール化したときの輝度取得
				int pixelAvg = (pixelBuffer[0] + pixelBuffer[1] + pixelBuffer[2]) / 3;
				// RGBをすべてに同値を設定することでグレースケール化する
				pixelBuffer[0] = pixelAvg;
				pixelBuffer[1] = pixelAvg;
				pixelBuffer[2] = pixelAvg;
				raster.setPixel(x, y, pixelBuffer);
			}
		}

		return copyImage;
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
	 * 二つの画像と、マーク情報からDiff比較用画像を返す
	 *
	 * @param leftImg 左側の画像
	 * @param rightImg 右側の画像
	 * @param diffPoints Diffの座標
	 * @return
	 */
	public static BufferedImage getDiffImage(BufferedImage leftImg, BufferedImage rightImg, List<Point> diffPoints) {

		if (diffPoints != null && diffPoints.isEmpty()) {
			return null;
		}

		if (diffPoints == null) {
			// TODO: 画像サイズが違うとdiffPointsがnullになる。一旦仮置きで並べた画像だけ表示
			diffPoints = new ArrayList<Point>();
		}

		// Diff画像の生成
		BufferedImage expectedBaseImage = getMarkedImage(leftImg, diffPoints);
		BufferedImage actualBaseImage = getMarkedImage(rightImg, diffPoints);
		int diffImageWidth = expectedBaseImage.getWidth() + actualBaseImage.getWidth();
		int diffImageHeight = (expectedBaseImage.getHeight() >= actualBaseImage.getHeight()) ? expectedBaseImage
				.getHeight() : actualBaseImage.getHeight();
		int statusHeight = 50; // expected/actualのラベルを表示する領域の高さ
		BufferedImage diffImage = new BufferedImage(diffImageWidth + 4, diffImageHeight + statusHeight + 2, 1); // 上下左右の枠1px分を幅、高さに含める
		Graphics2D g = (Graphics2D) diffImage.getGraphics();
		// 左右の差分画像（グレースケール）の描画
		g.drawImage(expectedBaseImage, 1, statusHeight + 1, null);
		g.drawImage(actualBaseImage, expectedBaseImage.getWidth() + 3, statusHeight + 1, null);
		// expected（左）のラベル領域と枠の描画
		g.setColor(new Color(0.2f, 0.5f, 1.0f, 1.0f)); // 青
		g.fillRect(0, 0, expectedBaseImage.getWidth() + 3, statusHeight + 1);
		g.drawRect(0, statusHeight, expectedBaseImage.getWidth() + 1, diffImageHeight + 1);
		// actual（右）のラベル領域と枠の描画
		g.setColor(new Color(0.8f, 0.2f, 0.2f, 1.0f)); // 赤
		g.fillRect(expectedBaseImage.getWidth() + 2, 0, actualBaseImage.getWidth() + 3, statusHeight + 1);
		g.drawRect(expectedBaseImage.getWidth() + 2, statusHeight, actualBaseImage.getWidth() + 1, diffImageHeight + 1);
		// ラベル文字の描画
		g.setColor(new Color(1.0f, 1.0f, 1.0f, 1.0f));
		g.setFont(new Font("Arial", Font.BOLD, 25));
		g.drawString("expected", expectedBaseImage.getWidth() / 2 - 80, 35);
		g.drawString("actual", expectedBaseImage.getWidth() + actualBaseImage.getWidth() / 2 - 80 + 3, 35);

		return diffImage;
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

	/**
	 * 二つの画像とdiff座標から、diff画像を作成する。
	 *
	 * @param leftImg 左側の画像イメージ
	 * @param rightImg 右側の画像イメージ
	 * @param diffPoints diff座標
	 * @param filePath 保存パス
	 * @throws IOException
	 */
	public static void saveDiffImage(BufferedImage leftImg, BufferedImage rightImg, List<Point> diffPoints,
			String filePath) throws IOException {
		saveImage(getDiffImage(leftImg, rightImg, diffPoints), filePath);
	}

	/**
	 * イメージを保存する
	 *
	 * @param img 保存するイメージ
	 * @param filePath 保存先パス
	 * @throws IOException
	 */
	private static void saveImage(BufferedImage img, String filePath) throws IOException {
		ImageIO.write(img, "png", new File(filePath + ".png"));
	}
}
