const mongoose = require('mongoose');

const eventSchema = new mongoose.Schema({
  title: {
    type: String,
    required: true,
    trim: true
  },
  description: {
    type: String,
    required: true
  },
  date: {
    type: Date,
    required: true
  },
  location: {
    type: String,
    required: true
  },
  organizer: {
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User',
    required: true
  },
  participants: [{
    type: mongoose.Schema.Types.ObjectId,
    ref: 'User'
  }],
  capacity: {
    type: Number,
    required: true,
    min: 1
  },
  price: {
    type: Number,
    required: true,
    min: 0,
    default: 0
  },
  availableTickets: {
    type: Number,
    required: true,
    min: 0
  },
  category: {
    type: String,
    required: true,
    validate: {
      validator: function(v) {
        const validCategories = ['social', 'business', 'sports', 'education', 'other'];
        return validCategories.some(cat => cat.toLowerCase() === v.toLowerCase());
      },
      message: props => `${props.value} is not a valid category. Must be one of: social, business, sports, education, other`
    }
  },
  status: {
    type: String,
    required: true,
    enum: ['upcoming', 'ongoing', 'completed', 'cancelled'],
    default: 'upcoming'
  },
  imageBase64: {
    type: String,
    required: false,
    validate: {
      validator: function(v) {
        // Basic validation for base64 image string
        return !v || v.startsWith('data:image/');
      },
      message: props => 'Invalid base64 image format'
    }
  }
}, {
  timestamps: true
});

// Middleware to set initial availableTickets equal to capacity if not provided
eventSchema.pre('save', function(next) {
  if (this.isNew && !this.availableTickets) {
    this.availableTickets = this.capacity;
  }
  next();
});

const Event = mongoose.model('Event', eventSchema);

module.exports = Event; 