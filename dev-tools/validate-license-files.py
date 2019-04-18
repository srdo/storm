#!/usr/bin/env python3

# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


from contextlib import contextmanager
from random import randint
from pathlib import Path
import os
import subprocess
import shlex
import filecmp
import shutil
import re
import itertools
import argparse

project_root = Path(__file__).resolve().parent.parent
update_dependency_licenses_cmd = ('mvn license:aggregate-add-third-party@generate-and-check-licenses' +
                                  ' -Dlicense.skipAggregateAddThirdParty=false')


@contextmanager
def cd(newdir):
    prevdir = Path.cwd()
    os.chdir(newdir.expanduser())
    try:
        yield
    finally:
        os.chdir(prevdir)


def generate_dependency_licenses():
    """Generates DEPENDENCY-LICENSES in target. The committed DEPENDENCY-LICENSES is not modified."""
    print('Generating DEPENDENCY-LICENSES')
    update_dependency_licenses_output_to_target_cmd = (update_dependency_licenses_cmd +
                                                       ' -Dlicense.thirdPartyFilename=DEPENDENCY-LICENSES' +
                                                       ' -Dlicense.outputDirectory=target')
    subprocess.check_call(shlex.split(
        update_dependency_licenses_output_to_target_cmd))


def check_dependency_licenses():
    """Regenerates DEPENDENCY-LICENSES, and verifies that it didn't change"""
    print('Checking DEPENDENCY-LICENSES')
    if (not filecmp.cmp(Path('DEPENDENCY-LICENSES'), Path('target') / 'DEPENDENCY-LICENSES', shallow=False)):
        print(
            "DEPENDENCY-LICENSES and target/DEPENDENCY-LICENSES are different. Please update DEPENDENCY-LICENSES by running '{}' in the project root".format(update_dependency_licenses_cmd))
        return False
    return True


def build_storm():
    print("Building Storm")
    subprocess.check_call(shlex.split(
        'mvn clean install -DskipTests'
    ))


def extract_license_report_maven_coordinates(lines):
    # Lines like " * Checker Qual (org.checkerframework:checker-qual:2.5.2 - https://checkerframework.org)"
    matches = map(lambda line: re.match(
        r'\s+\*.*\((?P<gav>.*) \- .*\).*', line), lines)
    return set(map(lambda match: match.group('gav'), filter(lambda match: match != None, matches)))


def parse_license_binary_dependencies_coordinate_set():
    """Gets the dependencies listed in LICENSE-binary"""
    license_binary_begin_binary_section = '----------------------------END OF SOURCE NOTICES -------------------------------------------'
    license_binary_lines = read_lines(project_root / 'LICENSE-binary')
    return extract_license_report_maven_coordinates(
        itertools.dropwhile(lambda line: license_binary_begin_binary_section not in line, license_binary_lines))


def extract_dependency_list_maven_coordinates(lines):
    # Lines like "   com.google.code.findbugs:jsr305:jar:3.0.2"
    matches = map(lambda line: re.match(
        r'\s+(?P<group>.*)\:(?P<artifact>.*)\:(?P<type>.*)\:(?P<version>.*)', line), lines)
    return set(map(lambda match: match.group('group') + ':' + match.group('artifact') + ':' + match.group('version'), filter(lambda match: match != None, matches)))


def read_lines(path):
    with open(path) as f:
        return f.readlines()


def generate_storm_dist_dependencies_coordinate_set():
    """Gets the dependencies for storm-dist/binary, plus the ones in storm-shaded-deps"""
    generated_coordinate_set = extract_license_report_maven_coordinates(read_lines(
        project_root / 'storm-dist' / 'binary' / 'target' / 'generated-sources' / 'license' / 'THIRD-PARTY.txt'))

    # Add dependencies from storm-shaded-deps
    with cd(project_root / 'storm-shaded-deps'):
        subprocess.check_call(shlex.split(
            'mvn dependency:list -DoutputFile=target/deps-list -Dmdep.outputScope=false -DincludeScope=compile'))
    shaded_dep_coordinates = extract_dependency_list_maven_coordinates(
        read_lines(project_root / 'storm-shaded-deps' / 'target' / 'deps-list'))
    print('The storm-shaded-deps dependencies that will be considered part of storm-dist/binary are ' + str(shaded_dep_coordinates))
    print('')
    generated_coordinate_set.update(shaded_dep_coordinates)

    return set(filter(lambda coordinate: 'org.apache.storm:' not in coordinate, generated_coordinate_set))

def generate_storm_dist_license_report():
    with cd(project_root / 'storm-dist' / 'binary'):
        print('')
        subprocess.check_call(shlex.split(
            'mvn license:aggregate-add-third-party@generate-and-check-licenses -Dlicense.skipAggregateAddThirdParty=false'))

def check_license_binary():
    """Checks that the dependencies in the storm-dist/binary report are mentioned in LICENSE-binary, and vice versa"""
    print('Checking LICENSE-binary')

    license_binary_coordinate_set = parse_license_binary_dependencies_coordinate_set()
    generated_coordinate_set = generate_storm_dist_dependencies_coordinate_set()
    superfluous_coordinates_in_license = license_binary_coordinate_set.difference(
        generated_coordinate_set)
    coordinates_missing_in_license = generated_coordinate_set.difference(
        license_binary_coordinate_set)
    if superfluous_coordinates_in_license:
        print('Dependencies in LICENSE-binary that appear unused: ')
        for coord in sorted(superfluous_coordinates_in_license):
            print(coord)
    print('')
    if coordinates_missing_in_license:
        print('Dependencies missing from LICENSE-binary: ')
        for coord in sorted(coordinates_missing_in_license):
            print(coord)
        return False
    return not coordinates_missing_in_license and not superfluous_coordinates_in_license


with cd(project_root):
    parser = argparse.ArgumentParser(description='Validate Storm license files are up to date (excluding NOTICE-binary and the licenses/ directory)')
    parser.add_argument('--skip-build-storm', action='store_true', help='set to skip building Storm')
    args = parser.parse_args()
    success = True

    if not args.skip_build_storm:
        build_storm()
    generate_dependency_licenses()
    generate_storm_dist_license_report()
    success = check_dependency_licenses() and success
    success = check_license_binary() and success
    if not success:
        exit(1)
    exit(0)
