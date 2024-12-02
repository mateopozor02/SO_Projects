import unittest
import numpy as np

from bankers_algo import is_safe_state

class TestBankersAlgorithm(unittest.TestCase):
    
    def test_safe_state(self):
        processes = ['P0', 'P1', 'P2', 'P3', 'P4']
        available = np.array([3, 3, 2])
        max_matrix = np.array([
            [7, 5, 3],
            [3, 2, 2],
            [9, 0, 2],
            [2, 2, 2],
            [4, 3, 3]
        ])
        allocation = np.array([
            [0, 1, 0],
            [2, 0, 0],
            [3, 0, 2],
            [2, 1, 1],
            [0, 0, 2]
        ])
        result, sequence = is_safe_state(processes, available, max_matrix, allocation)
        self.assertTrue(result)
        self.assertEqual(sequence, ['P1', 'P3', 'P4', 'P0', 'P2'])
    
    def test_unsafe_state(self):
        processes = ['P0', 'P1', 'P2']
        available = np.array([1, 0, 0])
        max_matrix = np.array([
            [1, 2, 2],
            [1, 0, 3],
            [1, 1, 1]
        ])
        allocation = np.array([
            [0, 1, 1],
            [1, 0, 0],
            [1, 1, 0]
        ])
        result, sequence = is_safe_state(processes, available, max_matrix, allocation)
        self.assertFalse(result)
        self.assertEqual(sequence, [])
    
    def test_single_process_safe(self):
        processes = ['P0']
        available = np.array([3])
        max_matrix = np.array([[5]])
        allocation = np.array([[2]])
        result, sequence = is_safe_state(processes, available, max_matrix, allocation)
        self.assertTrue(result)
        self.assertEqual(sequence, ['P0'])
        
    def test_no_available_resources(self):
        processes = ['P0', 'P1']
        available = np.array([0, 0])
        max_matrix = np.array([
            [2, 1],
            [1, 2]
        ])
        allocation = np.array([
            [1, 0],
            [1, 2]
        ])
        result, sequence = is_safe_state(processes, available, max_matrix, allocation)
        self.assertTrue(result)
        self.assertEqual(sequence, ['P1', 'P0'])

if __name__ == '__main__':
    unittest.main()
