const express = require('express');
const router = express.Router();
const eventController = require('../controllers/event.controller');
const auth = require('../middleware/auth'); // Assuming you have authentication middleware
const { upload, convertToBase64 } = require('../middleware/upload');

// Create a new event with image upload
router.post('/', auth, upload.single('image'), convertToBase64, eventController.createEvent);

// Get all events
router.get('/', eventController.getAllEvents);

// Get user's events
router.get('/my-events', auth, eventController.getUserEvents);

// Get event by ID
router.get('/:id', eventController.getEventById);

// Update event with image upload
router.put('/:id', auth, upload.single('image'), convertToBase64, eventController.updateEvent);

// Delete event
router.delete('/:id', auth, eventController.deleteEvent);

// Join event
router.post('/:id/join', auth, eventController.joinEvent);

module.exports = router; 