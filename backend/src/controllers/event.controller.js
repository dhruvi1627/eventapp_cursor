const Event = require('../models/event.model');

const createEvent = async (req, res) => {
  try {
    console.log('Request body:', req.body);
    console.log('Request file:', req.file);
    console.log('User ID:', req.user.id);

    let eventData;

    // Check if this is a multipart request with individual fields
    if (req.body.title && req.body.description) {
      // Handle multipart form data
      eventData = {
        title: req.body.title,
        description: req.body.description,
        date: new Date(req.body.date),
        location: req.body.location,
        capacity: parseInt(req.body.capacity),
        price: parseFloat(req.body.price),
        category: req.body.category.toLowerCase(),
        organizer: req.user.id
      };
    } else {
      // Handle JSON body (for requests without image)
      eventData = {
        ...req.body,
        organizer: req.user.id,
        category: req.body.category.toLowerCase()
      };
    }

    // Add image if present
    if (req.file && req.file.base64) {
      eventData.imageBase64 = req.file.base64;
    }

    // Set availableTickets equal to capacity
    eventData.availableTickets = eventData.capacity;

    console.log('Event data to save:', eventData);

    const event = new Event(eventData);
    const savedEvent = await event.save();

    // Always populate the organizer field before sending response
    // This ensures consistency whether the request includes an image or not
    const populatedEvent = await Event.findById(savedEvent._id)
      .populate('organizer', 'name email');

    res.status(201).json({
      success: true,
      message: 'Event created successfully',
      data: populatedEvent
    });

  } catch (error) {
    console.error('Error creating event:', error);
    
    if (error.name === 'ValidationError') {
      const errors = Object.values(error.errors).map(err => err.message);
      return res.status(400).json({
        success: false,
        message: 'Validation error',
        errors: errors
      });
    }

    res.status(500).json({
      success: false,
      message: 'Server error',
      error: error.message
    });
  }
};

const getAllEvents = async (req, res) => {
  try {
    const events = await Event.find({ status: 'upcoming' })
      .populate('organizer', 'name email')
      .sort({ date: 1 });

    res.json({
      success: true,
      data: events
    });
  } catch (error) {
    console.error('Error fetching events:', error);
    res.status(500).json({
      success: false,
      message: 'Server error',
      error: error.message
    });
  }
};

const getUserEvents = async (req, res) => {
  try {
    // Debug logs
    console.log('getUserEvents called');
    console.log('User ID from token:', req.user?.id);
    
    // Check if user is authenticated
    if (!req.user || !req.user.id) {
      return res.status(401).json({
        success: false,
        message: 'User not authenticated'
      });
    }

    // Find events where the current user is the organizer
    const events = await Event.find({ organizer: req.user.id })
      .populate('organizer', 'name email')
      .populate('participants', 'name email')
      .sort({ createdAt: -1 });

    console.log(`Found ${events.length} events for user ${req.user.id}`);

    res.json({
      success: true,
      message: `Found ${events.length} events`,
      data: events
    });

  } catch (error) {
    console.error('Error fetching user events:', error);
    console.error('Error stack:', error.stack);
    
    res.status(500).json({
      success: false,
      message: 'Server error while fetching events',
      error: process.env.NODE_ENV === 'development' ? error.message : 'Internal server error'
    });
  }
};
const getEventById = async (req, res) => {
  try {
    const event = await Event.findById(req.params.id)
      .populate('organizer', 'name email')
      .populate('participants', 'name email');

    if (!event) {
      return res.status(404).json({
        success: false,
        message: 'Event not found'
      });
    }

    res.json({
      success: true,
      data: event
    });
  } catch (error) {
    console.error('Error fetching event:', error);
    res.status(500).json({
      success: false,
      message: 'Server error',
      error: error.message
    });
  }
};

const updateEvent = async (req, res) => {
  try {
    const event = await Event.findById(req.params.id);

    if (!event) {
      return res.status(404).json({
        success: false,
        message: 'Event not found'
      });
    }

    // Check if user is the organizer
    if (event.organizer.toString() !== req.user.id) {
      return res.status(403).json({
        success: false,
        message: 'Not authorized to update this event'
      });
    }

    let updateData;
    
    // Check if this is a multipart request
    if (req.body.title && req.body.description) {
      updateData = {
        title: req.body.title,
        description: req.body.description,
        date: new Date(req.body.date),
        location: req.body.location,
        capacity: parseInt(req.body.capacity),
        price: parseFloat(req.body.price),
        category: req.body.category.toLowerCase()
      };
    } else {
      updateData = {
        ...req.body,
        category: req.body.category.toLowerCase()
      };
    }

    // Add image if present
    if (req.file && req.file.base64) {
      updateData.imageBase64 = req.file.base64;
    }

    const updatedEvent = await Event.findByIdAndUpdate(
      req.params.id,
      updateData,
      { new: true, runValidators: true }
    ).populate('organizer', 'name email');

    res.json({
      success: true,
      message: 'Event updated successfully',
      data: updatedEvent
    });
  } catch (error) {
    console.error('Error updating event:', error);
    
    if (error.name === 'ValidationError') {
      const errors = Object.values(error.errors).map(err => err.message);
      return res.status(400).json({
        success: false,
        message: 'Validation error',
        errors: errors
      });
    }

    res.status(500).json({
      success: false,
      message: 'Server error',
      error: error.message
    });
  }
};

const deleteEvent = async (req, res) => {
  try {
    const event = await Event.findById(req.params.id);

    if (!event) {
      return res.status(404).json({
        success: false,
        message: 'Event not found'
      });
    }

    // Check if user is the organizer
    if (event.organizer.toString() !== req.user.id) {
      return res.status(403).json({
        success: false,
        message: 'Not authorized to delete this event'
      });
    }

    await Event.findByIdAndDelete(req.params.id);

    res.json({
      success: true,
      message: 'Event deleted successfully'
    });
  } catch (error) {
    console.error('Error deleting event:', error);
    res.status(500).json({
      success: false,
      message: 'Server error',
      error: error.message
    });
  }
};

const joinEvent = async (req, res) => {
  try {
    const event = await Event.findById(req.params.id);

    if (!event) {
      return res.status(404).json({
        success: false,
        message: 'Event not found'
      });
    }

    // Check if user is already a participant
    if (event.participants.includes(req.user.id)) {
      return res.status(400).json({
        success: false,
        message: 'You are already registered for this event'
      });
    }

    // Check if event has available tickets
    if (event.availableTickets <= 0) {
      return res.status(400).json({
        success: false,
        message: 'Event is fully booked'
      });
    }

    // Add user to participants and decrease available tickets
    event.participants.push(req.user.id);
    event.availableTickets -= 1;

    await event.save();

    const updatedEvent = await Event.findById(req.params.id)
      .populate('organizer', 'name email')
      .populate('participants', 'name email');

    res.json({
      success: true,
      message: 'Successfully joined the event',
      data: updatedEvent
    });
  } catch (error) {
    console.error('Error joining event:', error);
    res.status(500).json({
      success: false,
      message: 'Server error',
      error: error.message
    });
  }
};

module.exports = {
  createEvent,
  getAllEvents,
  getUserEvents,
  getEventById,
  updateEvent,
  deleteEvent,
  joinEvent
};











// const Event = require('../models/event.model');

// // Create a new event
// exports.createEvent = async (req, res) => {
//   try {
//     const event = new Event({
//       ...req.body,
//       organizer: req.user.id,
//       availableTickets: req.body.capacity
//     });
//     await event.save();
    
//     // Populate the organizer details in the response
//     const populatedEvent = await Event.findById(event._id)
//       .populate('organizer', 'name email')
//       .populate('participants', 'name email');
      
//     res.status(201).json({
//       success: true,
//       message: 'Event created successfully',
//       data: populatedEvent
//     });
//   } catch (error) {
//     res.status(400).json({
//       success: false,
//       message: error.message
//     });
//   }
// };

// // Get all events
// exports.getAllEvents = async (req, res) => {
//   try {
//     const events = await Event.find()
//       .populate('organizer', 'name email')
//       .populate('participants', 'name email')
//       .sort({ date: 1 });
//     res.json({
//       success: true,
//       message: 'Events retrieved successfully',
//       data: events
//     });
//   } catch (error) {
//     res.status(500).json({
//       success: false,
//       message: error.message
//     });
//   }
// };

// // Get event by ID
// exports.getEventById = async (req, res) => {
//   try {
//     const event = await Event.findById(req.params.id)
//       .populate('organizer', 'name email')
//       .populate('participants', 'name email');
//     if (!event) {
//       return res.status(404).json({
//         success: false,
//         message: 'Event not found'
//       });
//     }
//     res.json({
//       success: true,
//       message: 'Event retrieved successfully',
//       data: event
//     });
//   } catch (error) {
//     res.status(500).json({
//       success: false,
//       message: error.message
//     });
//   }
// };

// // Update event
// exports.updateEvent = async (req, res) => {
//   try {
//     const event = await Event.findById(req.params.id);
//     if (!event) {
//       return res.status(404).json({
//         success: false,
//         message: 'Event not found'
//       });
//     }
    
//     // Check if user is the organizer
//     if (event.organizer.toString() !== req.user.id.toString()) {
//       return res.status(403).json({
//         success: false,
//         message: 'Only organizer can update event'
//       });
//     }

//     // If capacity is being updated, adjust availableTickets accordingly
//     if (req.body.capacity) {
//       const ticketDifference = req.body.capacity - event.capacity;
//       req.body.availableTickets = event.availableTickets + ticketDifference;
//     }

//     Object.assign(event, req.body);
//     await event.save();

//     // Populate the organizer details in the response
//     const updatedEvent = await Event.findById(event._id)
//       .populate('organizer', 'name email')
//       .populate('participants', 'name email');

//     res.json({
//       success: true,
//       message: 'Event updated successfully',
//       data: updatedEvent
//     });
//   } catch (error) {
//     res.status(400).json({
//       success: false,
//       message: error.message
//     });
//   }
// };

// // Delete event
// exports.deleteEvent = async (req, res) => {
//   try {
//     const event = await Event.findById(req.params.id);
//     if (!event) {
//       return res.status(404).json({
//         success: false,
//         message: 'Event not found'
//       });
//     }

//     // Check if user is the organizer
//     if (event.organizer.toString() !== req.user.id.toString()) {
//       return res.status(403).json({
//         success: false,
//         message: 'Only organizer can delete event'
//       });
//     }

//     await event.deleteOne();
//     res.json({
//       success: true,
//       message: 'Event deleted successfully'
//     });
//   } catch (error) {
//     res.status(500).json({
//       success: false,
//       message: error.message
//     });
//   }
// };

// // Join event
// exports.joinEvent = async (req, res) => {
//   try {
//     const event = await Event.findById(req.params.id);
//     if (!event) {
//       return res.status(404).json({
//         success: false,
//         message: 'Event not found'
//       });
//     }

//     // Check if user is already a participant
//     if (event.participants.includes(req.user.id)) {
//       return res.status(400).json({
//         success: false,
//         message: 'Already joined this event'
//       });
//     }

//     // Check if event has available tickets
//     if (event.availableTickets <= 0) {
//       return res.status(400).json({
//         success: false,
//         message: 'No tickets available'
//       });
//     }

//     event.participants.push(req.user.id);
//     event.availableTickets -= 1; // Decrease available tickets
//     await event.save();

//     // Populate the response
//     const updatedEvent = await Event.findById(event._id)
//       .populate('organizer', 'name email')
//       .populate('participants', 'name email');

//     res.json({
//       success: true,
//       message: 'Successfully joined event',
//       data: updatedEvent
//     });
//   } catch (error) {
//     res.status(400).json({
//       success: false,
//       message: error.message
//     });
//   }
// };

// // Get events created by user
// exports.getUserEvents = async (req, res) => {
//   try {
//     const events = await Event.find({ organizer: req.user.id })
//       .populate('organizer', 'name email')
//       .populate('participants', 'name email')
//       .sort({ date: 1 });
//     res.json({
//       success: true,
//       message: 'User events retrieved successfully',
//       data: events
//     });
//   } catch (error) {
//     res.status(500).json({
//       success: false,
//       message: error.message
//     });
//   }
// }; 