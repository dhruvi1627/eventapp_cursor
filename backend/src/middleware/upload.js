const multer = require('multer');
const path = require('path');

// Configure multer to store files in memory
const storage = multer.memoryStorage();

// File filter to accept only images
const fileFilter = (req, file, cb) => {
  const allowedFileTypes = /jpeg|jpg|png|gif|webp/;
  const extname = allowedFileTypes.test(path.extname(file.originalname).toLowerCase());
  const mimetype = allowedFileTypes.test(file.mimetype);

  if (mimetype && extname) {
    return cb(null, true);
  } else {
    cb(new Error('Only image files are allowed!'), false);
  }
};

// Multer configuration
const upload = multer({
  storage: storage,
  limits: {
    fileSize: 5 * 1024 * 1024, // 5MB limit
  },
  fileFilter: fileFilter
});

// Middleware to convert uploaded file to base64
const convertToBase64 = (req, res, next) => {
  if (req.file) {
    try {
      // Convert buffer to base64
      const base64String = req.file.buffer.toString('base64');
      
      // Create base64 data URL with proper MIME type
      const dataUrl = `data:${req.file.mimetype};base64,${base64String}`;
      
      // Add base64 property to req.file
      req.file.base64 = dataUrl;
      
      console.log('File converted to base64:', {
        originalname: req.file.originalname,
        mimetype: req.file.mimetype,
        size: req.file.size,
        base64Length: dataUrl.length
      });
    } catch (error) {
      console.error('Error converting file to base64:', error);
      return res.status(500).json({
        success: false,
        message: 'Error processing uploaded image'
      });
    }
  }
  next();
};

// Export the upload middleware with base64 conversion
const uploadSingle = (fieldName) => {
  return [
    upload.single(fieldName),
    convertToBase64
  ];
};

const uploadMultiple = (fieldName, maxCount) => {
  return [
    upload.array(fieldName, maxCount),
    (req, res, next) => {
      if (req.files && req.files.length > 0) {
        try {
          req.files.forEach(file => {
            const base64String = file.buffer.toString('base64');
            const dataUrl = `data:${file.mimetype};base64,${base64String}`;
            file.base64 = dataUrl;
          });
        } catch (error) {
          console.error('Error converting files to base64:', error);
          return res.status(500).json({
            success: false,
            message: 'Error processing uploaded images'
          });
        }
      }
      next();
    }
  ];
};

module.exports = {
  uploadSingle,
  uploadMultiple,
  upload,
  convertToBase64
};