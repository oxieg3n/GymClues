"""Run with: python -m unittest discover -s tools -p test_equipment_setups.py"""
import unittest
from equipment_setups import drawing


class EquipmentArtworkTest(unittest.TestCase):
    def test_cardio_does_not_fall_through_to_strength_machine(self):
        for scene in ("treadmill", "stairs", "bike", "rower"):
            with self.subTest(scene=scene):
                paths = [part[0] for part in drawing(scene)]
                self.assertNotIn("M100,335 H405 M195,330 V250 M290,330 V250 M155,322 L190,145", paths)
                self.assertNotIn("M470,330 V65 H562 V330 M455,332 H575", paths)


if __name__ == "__main__":
    unittest.main()
