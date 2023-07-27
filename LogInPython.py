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
