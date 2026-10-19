#!/usr/bin/env python3
"""Exits 0 if the given PE file carries an Authenticode signature, 1 if it does not.

`osslsigncode verify` also fails on an untrusted chain, which is the normal result for the
SignPath test certificate, so it cannot answer this question on its own. This reads the PE
Certificate Table data directory instead, which says whether a signature is present without
making any claim about its validity.
"""

import struct
import sys


def has_signature(path):
    with open(path, "rb") as f:
        data = f.read()

    pe = struct.unpack_from("<I", data, 0x3C)[0]
    if data[pe:pe + 4] != b"PE\0\0":
        raise ValueError("not a PE file: %s" % path)

    optional_header = pe + 24
    magic = struct.unpack_from("<H", data, optional_header)[0]
    if magic == 0x10B:
        directories = optional_header + 96
    elif magic == 0x20B:
        directories = optional_header + 112
    else:
        raise ValueError("unknown optional header magic 0x%x in %s" % (magic, path))

    # data directory entry 4 is the Certificate Table
    _, size = struct.unpack_from("<II", data, directories + 4 * 8)
    return size > 0


if __name__ == "__main__":
    signed = has_signature(sys.argv[1])
    print("signed" if signed else "unsigned")
    sys.exit(0 if signed else 1)
