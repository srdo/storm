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

project_root = Path(__file__).resolve().parent.parent


@contextmanager
def cd(newdir):
    prevdir = Path.cwd()
    os.chdir(newdir.expanduser())
    try:
        yield
    finally:
        os.chdir(prevdir)


def check_dependency_licenses():
    # Verify DEPENDENCY-LICENSES is up to date
    print('Checking DEPENDENCY-LICENSES')
    update_dependency_licenses_cmd = ('mvn license:aggregate-add-third-party@generate-and-check-licenses' +
                                      ' -Dlicense.skipAggregateAddThirdParty=false')
    update_dependency_licenses_output_to_target_cmd = (update_dependency_licenses_cmd +
                                                       ' -Dlicense.thirdPartyFilename=DEPENDENCY-LICENSES' +
                                                       ' -Dlicense.outputDirectory=target')

    subprocess.check_call(shlex.split(
        update_dependency_licenses_output_to_target_cmd))
    if (not filecmp.cmp(Path('DEPENDENCY-LICENSES'), Path('target') / 'DEPENDENCY-LICENSES', shallow=False)):
        print(
            f"DEPENDENCY-LICENSES and target/DEPENDENCY-LICENSES are different. Please update DEPENDENCY-LICENSES by running '{update_dependency_licenses_cmd}' in the project root")
        return False
    return True


def build_storm():
    print("Building Storm")
    subprocess.check_call(shlex.split(
        'mvn clean install -DskipTests'
    ))


def build_storm_dist_binary():
    print("Building storm-dist/binary")
    with cd(project_root / 'storm-dist' / 'binary'):
        subprocess.check_call(shlex.split('mvn clean package -Dgpg.skip'))


def extract_dependency_report_maven_coordinates(lines):
    # Lines like " * Checker Qual (org.checkerframework:checker-qual:2.5.2 - https://checkerframework.org)"
    matches = map(lambda line: re.match(
        r'\s+\*.*\((?P<gav>.*) \- .*\).*', line), lines)
    return set(map(lambda match: match.group('gav'), filter(lambda match: match != None, matches)))


def read_lines(path):
    with open(path) as f:
        return f.readlines()


# Checks that the dependencies in the storm-dist/binary report are mentioned in LICENSE-binary, and vice versa
def check_license_binary():
    print('Checking LICENSE-binary')
    with cd(project_root / 'storm-dist' / 'binary'):
        print('')
        #subprocess.check_call(shlex.split('mvn license:aggregate-add-third-party@generate-and-check-licenses -Dlicense.skipAggregateAddThirdParty=false'))
    generated_coordinate_set = extract_dependency_report_maven_coordinates(read_lines(
        project_root / 'storm-dist' / 'binary' / 'target' / 'generated-sources' / 'license' / 'THIRD-PARTY.txt'))
    generated_coordinate_set_without_storm_artifacts = set(filter(lambda coordinate: 'org.apache.storm:' not in coordinate, generated_coordinate_set))
        
    license_binary_begin_binary_section = '----------------------------END OF SOURCE NOTICES -------------------------------------------'
    license_binary_lines = read_lines(project_root / 'LICENSE-binary')
    license_binary_coordinate_set = extract_dependency_report_maven_coordinates(
        itertools.dropwhile(lambda line: license_binary_begin_binary_section not in line, license_binary_lines))
    superfluous_coordinates_in_license = license_binary_coordinate_set.difference(generated_coordinate_set_without_storm_artifacts)
    coordinates_missing_in_license = generated_coordinate_set_without_storm_artifacts.difference(license_binary_coordinate_set)
    if superfluous_coordinates_in_license:
        print('Dependencies in LICENSE-binary that appear unused (may be shaded?): ')
        # These don't fail the check, as storm-shaded-deps dependencies will appear here
        for coord in superfluous_coordinates_in_license:
            print(coord)
    print('')
    if coordinates_missing_in_license:
        print('Dependencies missing from LICENSE-binary: ')
        for coord in coordinates_missing_in_license:
            print(coord)
        return False
    return True



with cd(project_root):
    # check_dependency_licenses()
    # build_storm()
    check_license_binary()
    # build_storm_dist_binary()
