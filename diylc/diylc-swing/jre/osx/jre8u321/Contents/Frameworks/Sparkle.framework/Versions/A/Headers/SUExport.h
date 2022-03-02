//
//  SUExport.h
//  Sparkle
//
//  Created by Jake Petroules on 2014-08-23.
//  Copyright (c) 2014 Sparkle Project. All rights reserved.
//

/*
 * Copyright (c) 2020, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

#ifndef SUEXPORT_H
#define SUEXPORT_H

#ifdef SPARKLE_LITE
#define SU_EXPORT __attribute__((visibility("default")))
#else // SPARKLE_LITE
#ifdef BUILDING_SPARKLE
#define SU_EXPORT __attribute__((visibility("default")))
#else
#define SU_EXPORT
#endif
#endif // SPARKLE_LITE

#endif
