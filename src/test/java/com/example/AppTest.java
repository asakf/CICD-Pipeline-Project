package com.example;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class AppTest {

    private final App app = new App();

    @Test
    void testAdd() {
        assertEquals(5, app.add(2, 3));
    }

    @Test
    void testSubtract() {
        assertEquals(1, app.subtract(3, 2));
    }
}
