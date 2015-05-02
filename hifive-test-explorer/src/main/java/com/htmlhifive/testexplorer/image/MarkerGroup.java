/*
 * Copyright (C) 2015 NS Solutions Corporation, All Rights Reserved.
 */
package com.htmlhifive.testexplorer.image;

import java.awt.Point;
import java.awt.Rectangle;

/**
 * Diffの点の集合から作成されたマーカーモデル
 */
public class MarkerGroup {

	// グループ化可能な距離
	// 高速化メモリ節約のため、四角形を作るため誤差が出る
	private int distance = 10;

	// マーカーを付ける範囲を表す四角形
	private Rectangle rectangle;

	// 指定座標から結合可能距離。
	public MarkerGroup(Point p) {
		rectangle = new Rectangle(p.x - distance / 2, p.y - distance / 2, distance, distance);
	}

	public MarkerGroup(int x, int y) {
		this(new Point(x, y));
	}

	/**
	 * 二つのグループを一つにまとめる。事前に、まとめられるかを、isMargeで確認する必要あり
	 *
	 * @param markerGroup
	 */
	public void union(MarkerGroup markerGroup) {
		// 二つの四角形を結合する。事前に結合可能かチェックする。
		rectangle = rectangle.union(markerGroup.getRectangle());
	}

	/**
	 * 結合の条件を満たすかをチェックする。条件は内包または交差する。
	 *
	 * @param markerGroup_
	 * @return
	 */
	public boolean isMarge(MarkerGroup markerGroup_) {

		// お互いを内包している場合は結合可能
		if (markerGroup_.getRectangle().contains(this.getRectangle())
				|| this.getRectangle().contains(markerGroup_.getRectangle())) {
			return true;
		}

		// 交差している場合は結合可能
		if (markerGroup_.getRectangle().intersects(this.getRectangle())) {
			return true;
		}

		// それ以外は結合不可能
		return false;
	}

	/**
	 * 点の集合の四角形を返す
	 *
	 * @return
	 */
	public Rectangle getRectangle() {
		return rectangle;
	}

}
