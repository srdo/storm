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


@contextmanager
def cd(newdir):
    prevdir = Path.cwd()
    os.chdir(newdir.expanduser())
    try:
        yield
    finally:
        os.chdir(prevdir)


with cd(Path(__file__).resolve().parent.parent):
    # Verify DEPENDENCY-LICENSES is up to date
    update_dependency_licenses_cmd = ('mvn license:aggregate-add-third-party@generate-and-check-licenses' +
                                      ' -Dlicense.skipAggregateAddThirdParty=false')
    update_dependency_licenses_output_to_target_cmd = (update_dependency_licenses_cmd +
                                                       ' -Dlicense.thirdPartyFilename=DEPENDENCY-LICENSES' +
                                                       ' -Dlicense.outputDirectory=target')
    subprocess.run(shlex.split(update_dependency_licenses_output_to_target_cmd), check=True)
    if (not filecmp.cmp(Path('DEPENDENCY-LICENSES'), Path('target') / 'DEPENDENCY-LICENSES', shallow=False)):
        print(
            f"DEPENDENCY-LICENSES and target/DEPENDENCY-LICENSES are different. Please update DEPENDENCY-LICENSES by running '{update_dependency_licenses_cmd}'")
    
