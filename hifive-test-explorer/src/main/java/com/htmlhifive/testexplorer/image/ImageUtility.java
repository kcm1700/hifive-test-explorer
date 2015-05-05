/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.image;

import java.awt.BasicStroke;
import java.awt.Color;
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

public class ImageUtility {
	/**
	 * 2つの画像を比較し、一致しない領域の座標リストを返す。
	 */
	public static List<Rectangle> compare(BufferedImage img1, BufferedImage img2) {
		// 共通部分以外は、diff設定する
		final int beginX = Math.min(img1.getWidth(), img2.getWidth());
		final int endX = Math.max(img1.getWidth(), img2.getWidth());
		final int beginY = Math.min(img1.getHeight(), img2.getHeight());
		final int endY = Math.max(img1.getHeight(), img2.getHeight());

		final int capacity = (endX - beginX)*endY + (endY - beginY)*endX;
		ArrayList<Point> diffPoints = new ArrayList<Point>(capacity);
		for (int i = beginX; i < endX; ++i) { for (int j = 0; j < endY; ++j) { diffPoints.add(new Point(i, j)); } }
		for (int i = beginY; i < endY; ++i) { for (int j = 0; j < endX; ++j) { diffPoints.add(new Point(j, i)); } }

		// 共通部分のdiffを取得する
		final int width = Math.min(img1.getWidth(), img2.getWidth());
		final int height = Math.min(img1.getHeight(), img2.getHeight());
		final int[] expectedArr = img1.getRGB(0, 0, width, height, null, 0, width);
		final int[] actualArr = img2.getRGB(0, 0, width, height, null, 0, width);

		for (int i = 0; i < expectedArr.length; ++i) {
			if (expectedArr[i] == actualArr[i]) { continue; }
			diffPoints.add(new Point(i % width, i / width));
		}

		class MarkerGroup {
			// グループ化可能な距離。高速化メモリ節約のため、四角形を作るため誤差が出る。
			private static final int distance = 10;
			// マーカーを付ける範囲を表す四角形
			private Rectangle rectangle;
			// 指定座標から結合可能距離。
			public MarkerGroup(Point p) {
				rectangle = new Rectangle(p.x - distance / 2, p.y - distance / 2, distance, distance);
			}

			/**
			 * 二つのグループを一つにまとめる。事前に、まとめられるかを、isMargeで確認する必要あり。二つの四角形を結合する。事前に結合可能かチェックする。
			 */
			public void union(MarkerGroup param) {
				rectangle = rectangle.union(param.rectangle);
			}

			/**
			 * 結合の条件を満たすかをチェックする。条件は内包または交差する。
			 */
			public boolean isMarge(MarkerGroup param) {
				return param.rectangle.contains(rectangle) ||
						rectangle.contains(param.rectangle) ||
						param.rectangle.intersects(rectangle);
			}

			/**
			 * 点の集合の四角形を返す
			 */
			public Rectangle getRectangle() {
				return rectangle;
			}
		}


		ArrayList<MarkerGroup> diffGroups = new ArrayList<MarkerGroup>();
		for (Point point : diffPoints) {
			MarkerGroup markerGroup = new MarkerGroup(point);
			boolean flag = true;
			for (MarkerGroup diffGroup : diffGroups) {
				if (!diffGroup.isMarge(markerGroup)) { continue; }
				diffGroup.union(markerGroup);
				flag = false;
				break;
			}
			if (flag) { diffGroups.add(markerGroup); }
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
					for (MarkerGroup removeModel : removeList) { diffGroups.remove(removeModel); }
					break;
				}
			}
		}

		// diffGroupsからRectangleのリストを作成
		List<Rectangle> rectangles = new ArrayList<Rectangle>();
		for (MarkerGroup markerGroup : diffGroups) { rectangles.add(markerGroup.getRectangle()); }
		return rectangles;
	}

	/**
	 * @param img
	 * @param diffPoints
	 * @return
	 * @throws IOException
	 */
	public static BufferedImage getMarkedImage(BufferedImage img, List<Rectangle> diffRectangles) throws IOException {
		// マーカーの方が範囲が大きい場合、その範囲の画像を作成する
		int markerMaxX = img.getWidth();
		int markerMaxY = img.getHeight();
		int markerMinX = 0;
		int markerMinY = 0;

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

		BufferedImage markedImg;
		Graphics2D marker;
		if (isExtend) {
			//画像が、マーカーの範囲を超えている場合コピーする
			markedImg = new BufferedImage(markerMaxX - markerMinX, markerMaxY - markerMinY,
					BufferedImage.TYPE_INT_ARGB);
			marker = (Graphics2D) markedImg.getGraphics();
			marker.setBackground(Color.GRAY);
			marker.clearRect(0, 0, markerMaxX - markerMinX, markerMaxY - markerMinY);
			marker.drawImage(img, -markerMinX, -markerMinY, img.getWidth(), img.getHeight(), null);
		} else {
			ColorModel cm = img.getColorModel();
			boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
			WritableRaster raster = img.copyData(null);
			WritableRaster rasterChild = (WritableRaster) raster.createChild(0, 0, img.getWidth(), img.getHeight(),
					img.getMinX(), img.getMinY(), null);
			markedImg = new BufferedImage(cm, rasterChild, isAlphaPremultiplied, null);
			marker = (Graphics2D) markedImg.getGraphics();
		}

		marker.setColor(new Color(1.0f, 0.0f, 0.0f, 0.5f));
		marker.setStroke(new BasicStroke(4.0f));
		// 異なるピクセルの左上にマーカーを置いていく
		BufferedImage mark = ImageIO.read(AssertImage.class.getClassLoader().getResource("mark.png"));
		for (Rectangle markerGroup : diffRectangles) {
			marker.drawImage(mark, markerGroup.x - 30 - markerMinX, markerGroup.y - 25 - markerMinY, null);
			marker.drawRect(markerGroup.x - 2 - markerMinX, markerGroup.y - 2 - markerMinY, markerGroup.width + 4,
					markerGroup.height + 4);
		}

		return markedImg;
	}
}
