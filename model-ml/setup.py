from setuptools import find_packages
from setuptools import setup

REQUIRED_PACKAGES = [
    'gcsfs==0.7.1', 
    'dask[dataframe]==2022.2.0', 
    'google-cloud-bigquery-storage==1.0.0', 
    'six==1.15.0'
]
 
setup(
    name='trainer SIBI', 
    version='0.1', 
    install_requires=REQUIRED_PACKAGES,
    packages=find_packages(),
    include_package_data=True,
    description='Classification training flex sensor to SIBI classification model'
)