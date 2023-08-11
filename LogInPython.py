Since modules in Python are singletons (imported only once), you can access the same logger instance from other modules 
after it's set up in the main module.


import logging
import time
import inspect

def setup_logger(log_file_path):
    logging.basicConfig(
        level=logging.DEBUG,
        format='%(asctime)s - %(levelname)s - %(message)s',
        datefmt='%Y-%m-%d %H:%M:%S.%f',
        filename=log_file_path,
        filemode='a'
    )

def log_execution_time(func):
    def wrapper(*args, **kwargs):
        start_time = time.time()
        result = func(*args, **kwargs)
        end_time = time.time()
        execution_time = end_time - start_time
        hours, remainder = divmod(execution_time, 3600)
        minutes, seconds = divmod(remainder, 60)
        execution_time_str = f'{int(hours)}h {int(minutes)}m {seconds:.2f}s'
        logging.info(f'{get_formatted_timestamp()} - [{func.__name__}] - Execution time: {execution_time_str}')
        return result

    return wrapper

def log_info(message):
    function_name = inspect.stack()[1].function
    logging.info(f'{get_formatted_timestamp()} - [{function_name}] - {message}')

def log_warning(message):
    function_name = inspect.stack()[1].function
    logging.warning(f'{get_formatted_timestamp()} - [{function_name}] - {message}')

def log_error(message):
    function_name = inspect.stack()[1].function
    logging.error(f'{get_formatted_timestamp()} - [{function_name}] - {message}')

def log_critical(message):
    function_name = inspect.stack()[1].function
    logging.critical(f'{get_formatted_timestamp()} - [{function_name}] - {message}')

def get_formatted_timestamp():
    return time.strftime('%Y-%m-%d %H:%M:%S')


#--------------------------------


#!/bin/bash

# Specify the log file path (change it to your desired location)
log_file="path/to/your_log_file.log"

# Call the Python module with the log file argument
python your_module.py --log_file_path "$log_file" > output.log 2>&1


#--------------------------------
import argparse
import logger_utils

# Parse the command-line arguments
def parse_arguments():
    parser = argparse.ArgumentParser(description="Your Python Module")
    parser.add_argument("--log_file_path", type=str, help="Path to the log file")
    return parser.parse_args()

# Set up the logger based on the provided log file path
def setup_logger(log_file_path):
    logger_utils.setup_logger(log_file_path)

@logger_utils.log_execution_time
def your_function_to_log():
    # ... Your code or task execution ...

if __name__ == "__main__":
    args = parse_arguments()

    if args.log_file_path:
        setup_logger(args.log_file_path)
    else:
        # Provide a default log file path if not provided via command-line
        setup_logger("default_log_file.log")

    your_function_to_log()

Here the time execution logs captured by the logger_utils.log_execution_time decorator will be written to the log file specified by "$log_file".
Meanwhile, the standard output and any error messages will be redirected to "output.log" due to the shell redirection. 
The two types of logs will be separated in different files.




//////////



import logging
import gzip
from logging.handlers import GzippedRotatingFileHandler
from typing import Dict, Any
from pathlib import Path
from datetime import datetime

def setup_logging(config: Dict[str, Any]) -> None:
    # Log level
    verbosity = config.get('verbosity', 0)
    
    # Log to console
    if not config.get('logconsole', 0):
        logging.root.handlers = []
    
    # Logging to a file
    logfile = config.get('logfile', "what_if_analysis.log")
    if logfile:
        logdir = config.get("logdir", "./logs")
        create_folder(logdir)
        
        # Archive the existing log file
        archive_existing_log(logdir, logfile)
        
        # Configure GzippedRotatingFileHandler for rolling logs with gzip archival
        handler_rf = GzippedRotatingFileHandler(
            f"{logdir}/{logfile}",
            maxBytes=1024 * 1024 * 10,  # 10MB
            backupCount=10
        )
        handler_rf.setFormatter(logging.Formatter(LOGFORMAT))
        
        # Add the handler to root logger
        logging.root.addHandler(handler_rf)
        
    logging.root.setLevel(logging.INFO if verbosity < 1 else logging.DEBUG)
    _set_loggers(verbosity)

def archive_existing_log(logdir: str, logfile: str) -> None:
    existing_log = f"{logdir}/{logfile}"
    
    if Path(existing_log).is_file():
        timestamp = datetime.now().strftime("%Y-%m-%d_%H%M%S")
        archived_logfile = f"{logdir}/{logfile}.{timestamp}.log.gz"  # Use the same pattern
        
        try:
            # Compress the existing log file to a gzipped archive
            with open(existing_log, 'rb') as f_in, gzip.open(archived_logfile, 'wb') as f_out:
                f_out.writelines(f_in)
            
            # Remove the original log file
            Path(existing_log).unlink()
            
        except Exception as e:
            raise OperationalException(f"Error archiving existing log: {e}")
    else:
        logging.info("No existing log file to archive.")

def create_folder(path: str) -> None:
    try:
        logging.info(f"Checking if folder {path} exists")
        Path(path).mkdir(parents=True, exist_ok=True)
    except Exception as e:
        raise OperationalException(f"{e}\n" f"Folder creation {path} failed.")


/////////////

import unittest
import logging
import os
import gzip
from your_module import setup_logging, archive_existing_log  # Import your actual module functions

class TestLoggingSetup(unittest.TestCase):

    def test_setup_logging(self):
        config = {
            'verbosity': 1,
            'logconsole': False,
            'logfile': "test_log.log",
            'logdir': "./test_logs"
        }
        
        setup_logging(config)
        
        # Check if log directory and file are created
        self.assertTrue(os.path.exists(config['logdir']))
        self.assertTrue(os.path.exists(os.path.join(config['logdir'], config['logfile'])))
        
        # Clean up
        logging.root.handlers = []
        os.remove(os.path.join(config['logdir'], config['logfile']))

    def test_archive_existing_log(self):
        logdir = "./test_logs"
        logfile = "test_log.log"
        existing_logfile = os.path.join(logdir, logfile)
        
        # Create a dummy log file
        with open(existing_logfile, 'w') as f:
            f.write("Test log content")
        
        archive_existing_log(logdir, logfile)
        
        # Check if archived log file is created
        archived_logfile = os.path.join(logdir, f"{logfile}.archived.log.gz")
        self.assertTrue(os.path.exists(archived_logfile))
        
        # Check if archived log file can be decompressed and contains the original content
        with gzip.open(archived_logfile, 'rt') as f:
            archived_content = f.read()
            self.assertEqual(archived_content, "Test log content")
        
        # Clean up
        os.remove(existing_logfile)
        os.remove(archived_logfile)

if __name__ == '__main__':
    unittest.main()

//////////

import unittest
import logging
import os
from unittest.mock import Mock, patch
from your_module import setup_logging, archive_existing_log, create_folder  # Import your actual module functions

class TestLoggingSetup(unittest.TestCase):

    def setUp(self):
        self.config = {
            'verbosity': 1,
            'logconsole': False,
            'logfile': "test_log.log",
            'logdir': "./test_logs"
        }

    def test_setup_logging(self):
        with patch('builtins.open', create=True), \
             patch('gzip.open', create=True), \
             patch.object(Path, 'is_file', return_value=True), \
             patch.object(Path, 'unlink'), \
             patch('os.path.exists', return_value=True):

            setup_logging(self.config)

            # Check if log directory and file are created
            self.assertTrue(os.path.exists(self.config['logdir']))
            self.assertTrue(os.path.exists(os.path.join(self.config['logdir'], self.config['logfile'])))

    def test_archive_existing_log(self):
        with patch('builtins.open', create=True), \
             patch('gzip.open', create=True), \
             patch.object(Path, 'is_file', return_value=True), \
             patch.object(Path, 'unlink'), \
             patch('datetime.datetime') as mock_datetime:

            mock_datetime.now.return_value.strftime.return_value = "2023-08-15_123456"
            archive_existing_log(self.config['logdir'], self.config['logfile'])

            # Check if archived log file is created
            archived_logfile = os.path.join(self.config['logdir'], f"{self.config['logfile']}.2023-08-15_123456.log.gz")
            self.assertTrue(os.path.exists(archived_logfile))

    def test_create_folder(self):
        with patch('os.path.exists', return_value=False), \
             patch('os.makedirs'):

            create_folder(self.config['logdir'])

            # Check if folder creation is called with correct path
            os.makedirs.assert_called_with(self.config['logdir'], exist_ok=True)

if __name__ == '__main__':
    unittest.main()


////////


import unittest
import logging
import os
import gzip
from unittest.mock import Mock, patch
from pathlib import Path
from datetime import datetime

# Your original functions
def setup_logging(config):
    # ... (unchanged setup_logging function with the modified archive_existing_log)

def archive_existing_log(logdir, logfile):
    for i in range(10):  # Adjust the range based on your backupCount value
        existing_log = f"{logdir}/{logfile}.{i}"
        
        if Path(existing_log).is_file():
            timestamp = datetime.now().strftime("%Y-%m-%d_%H%M%S")
            archived_logfile = f"{logdir}/{logfile}.{i}.{timestamp}.log"
            gzipped_archived_logfile = f"{archived_logfile}.gz"  # Add .gz extension
            
            try:
                # Rename the existing log file
                Path(existing_log).rename(archived_logfile)
                
                # Compress the archived log file to a gzipped archive
                with open(archived_logfile, 'rb') as f_in, gzip.open(gzipped_archived_logfile, 'wb') as f_out:
                    f_out.writelines(f_in)
                
                # Remove the original archived log file
                Path(archived_logfile).unlink()
                
            except Exception as e:
                raise OperationalException(f"Error archiving existing log: {e}")
        else:
            break  # Exit loop if no more rolled over log files

def create_folder(path):
    try:
        logging.info(f"Checking if folder {path} exists")
        Path(path).mkdir(parents=True, exist_ok=True)
    except Exception as e:
        raise OperationalException(f"{e}\n" f"Folder creation {path} failed.")

# Unit tests
class TestLoggingSetup(unittest.TestCase):

    def setUp(self):
        self.config = {
            'verbosity': 1,
            'logconsole': False,
            'logfile': "test_log.log",
            'logdir': "./test_logs"
        }

    def test_setup_logging(self):
        with patch('builtins.open', create=True), \
             patch('gzip.open', create=True), \
             patch.object(Path, 'is_file', return_value=True), \
             patch.object(Path, 'unlink'), \
             patch('os.path.exists', return_value=True):

            setup_logging(self.config)

            # Check if log directory and file are created
            self.assertTrue(os.path.exists(self.config['logdir']))
            self.assertTrue(os.path.exists(os.path.join(self.config['logdir'], self.config['logfile'])))

    def test_archive_existing_log(self):
        with patch('builtins.open', create=True), \
             patch('gzip.open', create=True), \
             patch.object(Path, 'is_file', return_value=True), \
             patch.object(Path, 'unlink'), \
             patch('datetime.datetime') as mock_datetime:

            mock_datetime.now.return_value.strftime.return_value = "2023-08-15_123456"
            archive_existing_log(self.config['logdir'], self.config['logfile'])

            # Check if archived log file is created
            archived_logfile = os.path.join(self.config['logdir'], f"{self.config['logfile']}.0.2023-08-15_123456.log.gz")
            self.assertTrue(os.path.exists(archived_logfile))

    def test_create_folder(self):
        with patch('os.path.exists', return_value=False), \
             patch('os.makedirs'):

            create_folder(self.config['logdir'])

            # Check if folder creation is called with correct path
            os.makedirs.assert_called_with(self.config['logdir'], exist_ok=True)

if __name__ == '__main__':
    unittest.main()
