package ru.myitschool.spaceshooter;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class SpaceShooter extends ApplicationAdapter {
	public static final int SCR_WIDTH = 540, SCR_HEIGHT = 960; // размеры экрана
	long lastEnemySpawnTime; // время появления последнего вражеского корабля
	long enemySpawnInterval = 2000; // интервал между появлениями вражеских кораблей
	long lastShootTime; // время последнего выстрела
	long shootInterval = 1000; // интервал между выстрелами
	int nTrashes = 1000; // количество обломков при взрыве корабля

	SpriteBatch batch;
	OrthographicCamera camera; // камера для масштабирования под все разрешения экранов
	Vector3 touch; // объект для определения касаний

	// текстуры и звуки
	Texture imgShip;
	Texture imgStars;
	Texture imgShoot;
	Texture imgShipEnemy;
	Texture imgTrashEnemy;
	Texture imgTrashShip;
	Sound sndShoot;
	Sound sndExplosion;

	Array<Stars> stars = new Array<>(); // фон - звёздное небо
	Ship ship; // наш корабль
	Array<ShipEnemy> shipEnemies = new Array<>(); // вражеские корабли
	Array<Shoot> shoots = new Array<>(); // выстрелы
	Array<Trash> trashes = new Array<>(); // обломки вражеских кораблей
	Array<Trash> trashesShip = new Array<>(); // обломки нашего корабля

	@Override
	public void create() {
		batch = new SpriteBatch();
		camera = new OrthographicCamera();
		camera.setToOrtho(false, SCR_WIDTH, SCR_HEIGHT);
		touch = new Vector3();

		// загрузка картинок и звуков
		loadResources();

		stars.add(new Stars(0)); // создаём объекты неба
		stars.add(new Stars(SCR_HEIGHT)); // один объект неба над другим
		ship = new Ship();
	}

	@Override
	public void render() {
		actions();

		camera.update(); // обновляем камеру
		batch.setProjectionMatrix(camera.combined); // пересчитываем размеры всех объектов
		Gdx.gl.glClearColor(1, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.begin();
		for (int i = 0; i < stars.size; i++)
			batch.draw(imgStars, stars.get(i).x, stars.get(i).y, stars.get(i).width, stars.get(i).height);

		for (int i = 0; i < trashes.size; i++)
			batch.draw(imgTrashEnemy, trashes.get(i).x, trashes.get(i).y,
					trashes.get(i).width / 2, trashes.get(i).height / 2,
					trashes.get(i).width, trashes.get(i).height,
					1, 1, trashes.get(i).aRotation,
					0, 0, 100, 100, false, false);

		for (int i = 0; i < trashesShip.size; i++)
			batch.draw(imgTrashShip, trashesShip.get(i).x, trashesShip.get(i).y,
					trashesShip.get(i).width / 2, trashesShip.get(i).height / 2,
					trashesShip.get(i).width, trashesShip.get(i).height,
					1, 1, trashesShip.get(i).aRotation,
					0, 0, 100, 100, false, false);

		for (int i = 0; i < shipEnemies.size; i++)
			batch.draw(imgShipEnemy, shipEnemies.get(i).x, shipEnemies.get(i).y, shipEnemies.get(i).width, shipEnemies.get(i).height);

		for (int i = 0; i < shoots.size; i++)
			batch.draw(imgShoot, shoots.get(i).x, shoots.get(i).y, shoots.get(i).width, shoots.get(i).height);

		if (ship.isAlive) batch.draw(imgShip, ship.x, ship.y, ship.width, ship.height);
		batch.end();
	}

	private void actions() {
		// движение объектов неба
		for (int i = 0; i < stars.size; i++) stars.get(i).move();

		// обработка касаний экрана
		if (Gdx.input.isTouched()) {
			touch.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touch);
			ship.x += (touch.x - (ship.x + ship.width / 2)) / 20;
		}

		// порождение выстрелов, если корабль жив
		if (ship.isAlive) if (TimeUtils.millis() - lastShootTime > shootInterval) spawnShoot();

		// 	перемещение выстрелов
		for (int i = 0; i < shoots.size; i++) {
			shoots.get(i).move();
			// проверяем попадание выстрела в корабли врага
			for (int j = 0; j < shipEnemies.size; j++) {
				if (shoots.get(i).overlaps(shipEnemies.get(j))) {
					shoots.get(i).isAlive = false;
					shipEnemies.get(j).isAlive = false;
					sndExplosion.play();
					// порождается 100 обломков
					for (int k = 0; k < nTrashes; k++) {
						trashes.add(new Trash(shipEnemies.get(j)));
					}
				}
			}
			if (!shoots.get(i).isAlive) shoots.removeIndex(i); // удаляем из списка мёртвые выстрелы
		}

		// порождение вражеских кораблей
		if (TimeUtils.millis() - lastEnemySpawnTime > enemySpawnInterval) spawnEnemy();

		// перемещение вражеских кораблей
		for (int i = 0; i < shipEnemies.size; i++) {
			shipEnemies.get(i).move();
			if (shipEnemies.get(i).y < 0 && ship.isAlive) gameOver(); // если проравлись за край
			if (!shipEnemies.get(i).isAlive)
				shipEnemies.removeIndex(i); // удаляем из списка мёртвых врагов
		}

		// перемещение обломков врагов
		for (int i = 0; i < trashes.size; i++) {
			trashes.get(i).move();
			if (!trashes.get(i).isAlive) trashes.removeIndex(i);
		}

		// перемещение обломков нашего корабля
		for (int i = 0; i < trashesShip.size; i++) {
			trashesShip.get(i).move();
			if (!trashesShip.get(i).isAlive) trashesShip.removeIndex(i);
		}
	}

	// порождение выстрела
	void spawnShoot() {
		shoots.add(new Shoot(ship));
		lastShootTime = TimeUtils.millis();
		sndShoot.play();
	}

	// порождение вражеского корабля
	void spawnEnemy() {
		shipEnemies.add(new ShipEnemy());
		lastEnemySpawnTime = TimeUtils.millis();
	}

	// конец игры
	void gameOver() {
		ship.isAlive = false;
		sndExplosion.play();
		for (int k = 0; k < nTrashes; k++) {
			trashesShip.add(new Trash(ship));
		}
	}

	// загрузка картинок и звуков
	private void loadResources() {
		// загружаем текстуры
		imgShip = new Texture("ship.png");
		imgShipEnemy = new Texture("shipenemy.png");
		imgStars = new Texture("stars.png");
		imgShoot = new Texture("shoot.png");
		imgTrashEnemy = new Texture("trashenemy.png");
		imgTrashShip = new Texture("trashship.png");

		// загружаем звуки
		sndShoot = Gdx.audio.newSound(Gdx.files.internal("blaster.wav"));
		sndExplosion = Gdx.audio.newSound(Gdx.files.internal("explosion.wav"));
	}

	// очистка памяти от картинок и звуков
	@Override
	public void dispose() {
		batch.dispose();
		imgShip.dispose();
		imgStars.dispose();
		imgShoot.dispose();
		imgTrashEnemy.dispose();
		imgTrashShip.dispose();
		sndShoot.dispose();
		sndExplosion.dispose();
	}
}