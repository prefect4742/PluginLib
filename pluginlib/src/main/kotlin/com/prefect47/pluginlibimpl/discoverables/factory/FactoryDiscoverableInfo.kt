/*
 * Copyright (C) 2016 The Android Open Source Project
 * Copyright (C) 2018 Niklas Brunlid
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.prefect47.pluginlibimpl.discoverables.factory

import com.prefect47.pluginlib.DiscoverableInfo
import com.prefect47.pluginlib.factory.DiscoverableFactory

interface FactoryDiscoverableInfo: DiscoverableInfo {
    interface Factory: DiscoverableInfo.Factory<FactoryDiscoverableInfo>
    interface Listener: DiscoverableInfo.Listener<FactoryDiscoverableInfo>

    val factory: DiscoverableFactory
}
