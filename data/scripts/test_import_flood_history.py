import unittest
from import_flood_history import validate_row


class FloodImportValidationTest(unittest.TestCase):
    def row(self, **changes):
        return dict(location_label='Intersection', event_date='2024-08-13',
                    source_name='Publisher', source_url='https://example.com/event',
                    lat='21.03', lng='105.85', location_precision_m='100',
                    reviewed='true', geocode_method='Checked OSM intersection', depth_cm='', **changes)

    def test_unreviewed_locations_are_never_imported(self):
        row = self.row()
        row.update(reviewed='false', lat='', lng='')
        self.assertIsNone(validate_row(row))

    def test_missing_depth_stays_unknown(self):
        self.assertIsNone(validate_row(self.row())[6])

    def test_homepage_is_not_event_evidence(self):
        row = self.row()
        row['source_url'] = 'https://example.com/'
        with self.assertRaises(ValueError):
            validate_row(row)

    def test_outside_area_or_nonfinite_coordinate_is_rejected(self):
        for latitude in ('10.5', 'nan'):
            row = self.row()
            row['lat'] = latitude
            with self.assertRaises(ValueError):
                validate_row(row)

    def test_future_event_is_not_historical(self):
        row = self.row()
        row['event_date'] = '2999-01-01'
        with self.assertRaises(ValueError):
            validate_row(row)


if __name__ == '__main__':
    unittest.main()
