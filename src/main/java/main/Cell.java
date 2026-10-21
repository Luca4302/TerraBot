package main;

class Cell {

    Soil soil;
    Plants plant;
    Animal animal;
    Water water;
    Air air;


    public Cell(Soil soil, Plants plant, Animal animal, Water water, Air air) {
        this.soil = soil;
        this.plant = plant;
        this.animal = animal;
        this.water = water;
        this.air = air;
    }

}//posibila exceptie teren negasit apa etc
