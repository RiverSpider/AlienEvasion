package ru.myitschool.spaceshooter;

import com.badlogic.gdx.math.MathUtils;

public class SpaceObject {
    float x, y; // координаты
    int width, height;
    boolean isAlive = true;
    float vx, vy; // скорость

    // метод перемещения
    void move() {
        x += vx;
        y += vy;
        // проверка вылета за экран
        if (x < 0 - width || x > SpaceShooter.SCR_WIDTH || y < 0 - height || y > SpaceShooter.SCR_HEIGHT)
            isAlive = false;
    }

    // метод определения пересечения 2-х объектов
    boolean overlaps(SpaceObject o) {
        return (x > o.x && x < o.x + o.width || o.x > x && o.x < x + width) &&
                (y > o.y && y < o.y + o.height || o.y > y && o.y < y + height);
    }
}

class Trash extends SpaceObject {
    float aRotation, vRotation; // угол и скорость вращения

    // конструктор пнинимает объект, чтобы обломок появлялся в координатах этого объекта
    Trash(SpaceObject o) {
        float a; // угол, под которым полетит обломок
        float v; // длина вектора скорости
        x = o.x;
        y = o.y;
        width = MathUtils.random(10, 20);
        height = width;
        a = MathUtils.random(0, 360);
        v = MathUtils.random(1f, 10f);
        vx = v * MathUtils.sinDeg(a);
        vy = v * MathUtils.cosDeg(a);
        vRotation = MathUtils.random(-10, 10);
    }

    @Override
    void move() {
        super.move();
        // вращение
        aRotation += vRotation;
    }
}

// класс выстрела
class Shoot extends SpaceObject {
    // конструктор принимает класс корабля, чтобы выстрел появлялся там же
    Shoot(SpaceObject ship) {
        width = 100;
        height = 100;
        x = ship.x;
        y = ship.y;
        vy = 8;
    }
}

// класс вражеского корабля
class ShipEnemy extends SpaceObject {
    ShipEnemy() {
        width = 100;
        height = 100;
        // случайное появление по х за пределами экрана по y
        x = MathUtils.random(0, SpaceShooter.SCR_WIDTH - width);
        y = SpaceShooter.SCR_HEIGHT;
        vy = MathUtils.random(-5, -2);
    }
}

// класс нашего корабля
class Ship extends SpaceObject {
    Ship() {
        width = 100;
        height = 100;
        x = SpaceShooter.SCR_WIDTH / 2f - width / 2;
        y = 30;
    }
}

// класс звёздного неба в качестве фона
class Stars extends SpaceObject {
    Stars(float y) {
        width = SpaceShooter.SCR_WIDTH;
        height = SpaceShooter.SCR_HEIGHT;
        vy = -1;
        this.y = y;
    }
    @Override
    void move() {
        super.move(); // вызываем метод move из родительского класса
        if (y <= -height) y = height;
    }
}