/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2011
//
// bam.js: indexed binary alignments
//

var BAM_MAGIC = 21840194;
var BAI_MAGIC = 21578050;

function BamFile() {
}

function Vob(b, o) {
    this.block = b;
    this.offset = o;
}

Vob.prototype.toString = function() {
    return '' + this.block + ':' + this.offset;
}

function Chunk(minv, maxv) {
    this.minv = minv; this.maxv = maxv;
}

function makeBam(data, bai, callback) {
    var bam = new BamFile();
    bam.data = data;
    bam.bai = bai;

    bam.data.slice(0, 65536).fetch(function(r) {
        if (!r) {
            return dlog("Couldn't access BAM");
        }

        var unc = unbgzf(r);
        var uncba = new Uint8Array(unc);

        var magic = readInt(uncba, 0);
        var headLen = readInt(uncba, 4);
        var header = '';
        for (var i = 0; i < headLen; ++i) {
            header += String.fromCharCode(uncba[i + 8]);
        }

        var nRef = readInt(uncba, headLen + 8);
        var p = headLen + 12;

        bam.chrToIndex = {};
        bam.indexToChr = [];
        for (var i = 0; i < nRef; ++i) {
            var lName = readInt(uncba, p);
            var name = '';
            for (var j = 0; j < lName-1; ++j) {
                name += String.fromCharCode(uncba[p + 4 + j]);
            }
            var lRef = readInt(uncba, p + lName + 4);
            // dlog(name + ': ' + lRef);
            bam.chrToIndex[name] = i;
            if (name.indexOf('chr') == 0) {
                bam.chrToIndex[name.substring(3)] = i;
            } else {
                bam.chrToIndex['chr' + name] = i;
            }
            bam.indexToChr.push(name);

            p = p + 8 + lName;
        }

        if (bam.indices) {
            return callback(bam);
        }
    });

    bam.bai.fetch(function(header) {   // Do we really need to fetch the whole thing? :-(
        if (!header) {
            return dlog("Couldn't access BAI");
        }

        var uncba = new Uint8Array(header);
        var baiMagic = readInt(uncba, 0);
        if (baiMagic != BAI_MAGIC) {
            return dlog('Not a BAI file');
        }

        var nref = readInt(uncba, 4);

        bam.indices = [];

        var p = 8;
        for (var ref = 0; ref < nref; ++ref) {
            var blockStart = p;
            var nbin = readInt(uncba, p); p += 4;
            for (var b = 0; b < nbin; ++b) {
                var bin = readInt(uncba, p);
                var nchnk = readInt(uncba, p+4);
                p += 8 + (nchnk * 16);
            }
            var nintv = readInt(uncba, p); p += 4;
            p += (nintv * 8);
            if (nbin > 0) {
                bam.indices[ref] = new Uint8Array(header, blockStart, p - blockStart);
            }                     
        }
        if (bam.chrToIndex) {
            return callback(bam);
        }
    });
}



BamFile.prototype.blocksForRange = function(refId, min, max) {
    var index = this.indices[refId];
    if (!index) {
        return [];
    }

    var intBinsL = reg2bins(min, max);
    var intBins = [];
    for (var i = 0; i < intBinsL.length; ++i) {
        intBins[intBinsL[i]] = true;
    }
    var leafChunks = [], otherChunks = [];

    var nbin = readInt(index, 0);
    var p = 4;
    for (var b = 0; b < nbin; ++b) {
        var bin = readInt(index, p);
        var nchnk = readInt(index, p+4);
//        dlog('bin=' + bin + '; nchnk=' + nchnk);
        p += 8;
        if (intBins[bin]) {
            for (var c = 0; c < nchnk; ++c) {
                var cs = readVob(index, p);
                var ce = readVob(index, p + 8);
                (bin < 4681 ? otherChunks : leafChunks).push(new Chunk(cs, ce));
                p += 16;
            }
        } else {
            p +=  (nchnk * 16);
        }
    }
//    dlog('leafChunks = ' + miniJSONify(leafChunks));
//    dlog('otherChunks = ' + miniJSONify(otherChunks));

    var nintv = readInt(index, p);
    var lowest = null;
    var minLin = Math.min(min>>14, nintv - 1), maxLin = Math.min(max>>14, nintv - 1);
    for (var i = minLin; i <= maxLin; ++i) {
        var lb =  readVob(index, p + 4 + (i * 8));
        if (!lb) {
            continue;
        }
        if (!lowest || lb.block < lowest.block || lb.offset < lowest.offset) {
            lowest = lb;
        }
    }
    // dlog('Lowest LB = ' + lowest);
    
    var prunedOtherChunks = [];
    if (lowest != null) {
        for (var i = 0; i < otherChunks.length; ++i) {
            var chnk = otherChunks[i];
            if (chnk.maxv.block >= lowest.block && chnk.maxv.offset >= lowest.offset) {
                prunedOtherChunks.push(chnk);
            }
        }
    }
    // dlog('prunedOtherChunks = ' + miniJSONify(prunedOtherChunks));
    otherChunks = prunedOtherChunks;

    var intChunks = [];
    for (var i = 0; i < otherChunks.length; ++i) {
        intChunks.push(otherChunks[i]);
    }
    for (var i = 0; i < leafChunks.length; ++i) {
        intChunks.push(leafChunks[i]);
    }

    intChunks.sort(function(c0, c1) {
        var dif = c0.minv.block - c1.minv.block;
        if (dif != 0) {
            return dif;
        } else {
            return c0.minv.offset - c1.minv.offset;
        }
    });
    var mergedChunks = [];
    if (intChunks.length > 0) {
        var cur = intChunks[0];
        for (var i = 1; i < intChunks.length; ++i) {
            var nc = intChunks[i];
            if (nc.minv.block == cur.maxv.block /* && nc.minv.offset == cur.maxv.offset */) { // no point splitting mid-block
                cur = new Chunk(cur.minv, nc.maxv);
            } else {
                mergedChunks.push(cur);
                cur = nc;
            }
        }
        mergedChunks.push(cur);
    }
//    dlog('mergedChunks = ' + miniJSONify(mergedChunks));

    return mergedChunks;
}

BamFile.prototype.fetch = function(chr, min, max, callback) {
    var thisB = this;

    var chrId = this.chrToIndex[chr];
    var chunks;
    if (chrId === undefined) {
        chunks = [];
    } else {
        chunks = this.blocksForRange(chrId, min, max);
        if (!chunks) {
            callback(null, 'Error in index fetch');
        }
    }
    
    var records = [];
    var index = 0;
    var data;

    function tramp() {
        if (index >= chunks.length) {
            return callback(records);
        } else if (!data) {
            // dlog('fetching ' + index);
            var c = chunks[index];
            var fetchMin = c.minv.block;
            var fetchMax = c.maxv.block + (1<<16); // *sigh*
            thisB.data.slice(fetchMin, fetchMax - fetchMin).fetch(function(r) {
                data = unbgzf(r, c.maxv.block - c.minv.block + 1);
                return tramp();
            });
        } else {
            var ba = new Uint8Array(data);
            thisB.readBamRecords(ba, chunks[index].minv.offset, records, min, max, chrId);
            data = null;
            ++index;
            return tramp();
        }
    }
    tramp();
}

var SEQRET_DECODER = ['=', 'A', 'C', 'x', 'G', 'x', 'x', 'x', 'T', 'x', 'x', 'x', 'x', 'x', 'x', 'N'];
var CIGAR_DECODER = ['M', 'I', 'D', 'N', 'S', 'H', 'P', '=', 'X', '?', '?', '?', '?', '?', '?', '?'];

function BamRecord() {
}

BamFile.prototype.readBamRecords = function(ba, offset, sink, min, max, chrId) {
    while (true) {
        var blockSize = readInt(ba, offset);
        var blockEnd = offset + blockSize + 4;
        if (blockEnd >= ba.length) {
            return sink;
        }

        var record = new BamRecord();

        var refID = readInt(ba, offset + 4);
        var pos = readInt(ba, offset + 8);
        
        var bmn = readInt(ba, offset + 12);
        var bin = (bmn & 0xffff0000) >> 16;
        var mq = (bmn & 0xff00) >> 8;
        var nl = bmn & 0xff;

        var flag_nc = readInt(ba, offset + 16);
        var flag = (flag_nc & 0xffff0000) >> 16;
        var nc = flag_nc & 0xffff;
    
        var lseq = readInt(ba, offset + 20);
        
        var nextRef  = readInt(ba, offset + 24);
        var nextPos = readInt(ba, offset + 28);
        
        var tlen = readInt(ba, offset + 32);
    
        var readName = '';
        for (var j = 0; j < nl-1; ++j) {
            readName += String.fromCharCode(ba[offset + 36 + j]);
        }
    
        var p = offset + 36 + nl;

        var cigar = '';
        for (var c = 0; c < nc; ++c) {
            var cigop = readInt(ba, p);
            cigar = cigar + (cigop>>4) + CIGAR_DECODER[cigop & 0xf];
            p += 4;
        }
        record.cigar = cigar;
    
        var seq = '';
        var seqBytes = (lseq + 1) >> 1;
        for (var j = 0; j < seqBytes; ++j) {
            var sb = ba[p + j];
            seq += SEQRET_DECODER[(sb & 0xf0) >> 4];
            seq += SEQRET_DECODER[(sb & 0x0f)];
        }
        p += seqBytes;
        record.seq = seq;

        var qseq = '';
        for (var j = 0; j < lseq; ++j) {
            qseq += String.fromCharCode(ba[p + j]);
        }
        p += lseq;
        record.quals = qseq;
        
        record.pos = pos;
        record.mq = mq;
        record.readName = readName;
        record.segment = this.indexToChr[refID];

        while (p < blockEnd) {
            var tag = String.fromCharCode(ba[p]) + String.fromCharCode(ba[p + 1]);
            var type = String.fromCharCode(ba[p + 2]);
            var value;

            if (type == 'A') {
                value = String.fromCharCode(ba[p + 3]);
                p += 4;
            } else if (type == 'i' || type == 'I') {
                value = readInt(ba, p + 3);
                p += 7;
            } else if (type == 'c' || type == 'C') {
                value = ba[p + 3];
                p += 4;
            } else if (type == 's' || type == 'S') {
                value = readShort(ba, p + 3);
                p += 5;
            } else if (type == 'f') {
                throw 'FIXME need floats';
            } else if (type == 'Z') {
                p += 3;
                value = '';
                for (;;) {
                    var cc = ba[p++];
                    if (cc == 0) {
                        break;
                    } else {
                        value += String.fromCharCode(cc);
                    }
                }
            } else {
                throw 'Unknown type '+ type;
            }
            record[tag] = value;
        }

        if (!min || record.pos <= max && record.pos + lseq >= min) {
            if (chrId === undefined || refID == chrId) {
                sink.push(record);
            }
        }
        offset = blockEnd;
    }

    // Exits via top of loop.
}

function readInt(ba, offset) {
    return (ba[offset + 3] << 24) | (ba[offset + 2] << 16) | (ba[offset + 1] << 8) | (ba[offset]);
}

function readShort(ba, offset) {
    return (ba[offset + 1] << 8) | (ba[offset]);
}

function readVob(ba, offset) {
    var block = ((ba[offset+6] & 0xff) * 0x100000000) + ((ba[offset+5] & 0xff) * 0x1000000) + ((ba[offset+4] & 0xff) * 0x10000) + ((ba[offset+3] & 0xff) * 0x100) + ((ba[offset+2] & 0xff));
    var bint = (ba[offset+1] << 8) | (ba[offset]);
    if (block == 0 && bint == 0) {
        return null;  // Should only happen in the linear index?
    } else {
        return new Vob(block, bint);
    }
}

function unbgzf(data, lim) {
    lim = Math.min(lim || 1, data.byteLength - 100);
    var oBlockList = [];
    var ptr = [0];
    var totalSize = 0;

    while (ptr[0] < lim) {
        var ba = new Uint8Array(data, ptr[0], 100); // FIXME is this enough for all credible BGZF block headers?
        var xlen = (ba[11] << 8) | (ba[10]);
        // dlog('xlen[' + (ptr[0]) +']=' + xlen);
        var unc = jszlib_inflate_buffer(data, 12 + xlen + ptr[0], Math.min(65536, data.byteLength - 12 - xlen - ptr[0]), ptr);
        ptr[0] += 8;
        totalSize += unc.byteLength;
        oBlockList.push(unc);
    }

    if (oBlockList.length == 1) {
        return oBlockList[0];
    } else {
        var out = new Uint8Array(totalSize);
        var cursor = 0;
        for (var i = 0; i < oBlockList.length; ++i) {
            var b = new Uint8Array(oBlockList[i]);
            arrayCopy(b, 0, out, cursor, b.length);
            cursor += b.length;
        }
        return out.buffer;
    }
}

//
// Binning (transliterated from SAM1.3 spec)
//

/* calculate bin given an alignment covering [beg,end) (zero-based, half-close-half-open) */
function reg2bin(beg, end)
{
    --end;
    if (beg>>14 == end>>14) return ((1<<15)-1)/7 + (beg>>14);
    if (beg>>17 == end>>17) return ((1<<12)-1)/7 + (beg>>17);
    if (beg>>20 == end>>20) return ((1<<9)-1)/7 + (beg>>20);
    if (beg>>23 == end>>23) return ((1<<6)-1)/7 + (beg>>23);
    if (beg>>26 == end>>26) return ((1<<3)-1)/7 + (beg>>26);
    return 0;
}

/* calculate the list of bins that may overlap with region [beg,end) (zero-based) */
var MAX_BIN = (((1<<18)-1)/7);
function reg2bins(beg, end) 
{
    var i = 0, k, list = [];
    --end;
    list.push(0);
    for (k = 1 + (beg>>26); k <= 1 + (end>>26); ++k) list.push(k);
    for (k = 9 + (beg>>23); k <= 9 + (end>>23); ++k) list.push(k);
    for (k = 73 + (beg>>20); k <= 73 + (end>>20); ++k) list.push(k);
    for (k = 585 + (beg>>17); k <= 585 + (end>>17); ++k) list.push(k);
    for (k = 4681 + (beg>>14); k <= 4681 + (end>>14); ++k) list.push(k);
    return list;
}/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2010
//
// bigwig.js: indexed binary WIG (and BED) files
//

var BIG_WIG_MAGIC = -2003829722;
var BIG_BED_MAGIC = -2021002517;

var BIG_WIG_TYPE_GRAPH = 1;
var BIG_WIG_TYPE_VSTEP = 2;
var BIG_WIG_TYPE_FSTEP = 3;
    
function BigWig() {
}

BigWig.prototype.readChromTree = function(callback) {
    var thisB = this;
    this.chromsToIDs = {};
    this.idsToChroms = {};
    this.maxID = 0;

    var udo = this.unzoomedDataOffset;
    var eb = (udo - this.chromTreeOffset) & 3;
    udo = udo + 4 - eb;

    this.data.slice(this.chromTreeOffset, udo - this.chromTreeOffset).fetch(function(bpt) {
        var ba = new Uint8Array(bpt);
        var sa = new Int16Array(bpt);
        var la = new Int32Array(bpt);
        var bptMagic = la[0];
        var blockSize = la[1];
        var keySize = la[2];
        var valSize = la[3];
        var itemCount = (la[4] << 32) | (la[5]);
        var rootNodeOffset = 32;
        
        // console.log('blockSize=' + blockSize + '    keySize=' + keySize + '   valSize=' + valSize + '    itemCount=' + itemCount);

        var bptReadNode = function(offset) {
            var nodeType = ba[offset];
            var cnt = sa[(offset/2) + 1];
            // console.log('ReadNode: ' + offset + '     type=' + nodeType + '   count=' + cnt);
            offset += 4;
            for (var n = 0; n < cnt; ++n) {
                if (nodeType == 0) {
                    offset += keySize;
                    var childOffset = (la[offset/4] << 32) | (la[offset/4 + 1]);
                    offset += 8;
                    childOffset -= thisB.chromTreeOffset;
                    bptReadNode(childOffset);
                } else {
                    var key = '';
                    for (var ki = 0; ki < keySize; ++ki) {
                        var charCode = ba[offset++];
                        if (charCode != 0) {
                            key += String.fromCharCode(charCode);
                        }
                    }
                    var chromId = (ba[offset+3]<<24) | (ba[offset+2]<<16) | (ba[offset+1]<<8) | (ba[offset+0]);
                    var chromSize = (ba[offset + 7]<<24) | (ba[offset+6]<<16) | (ba[offset+5]<<8) | (ba[offset+4]);
                    offset += 8;

                    // console.log(key + ':' + chromId + ',' + chromSize);
                    thisB.chromsToIDs[key] = chromId;
                    if (key.indexOf('chr') == 0) {
                        thisB.chromsToIDs[key.substr(3)] = chromId;
                    }
                    thisB.idsToChroms[chromId] = key;
                    thisB.maxID = Math.max(thisB.maxID, chromId);
                }
            }
        };
        bptReadNode(rootNodeOffset);

        callback(thisB);
    });
}

function BigWigView(bwg, cirTreeOffset, cirTreeLength, isSummary) {
    this.bwg = bwg;
    this.cirTreeOffset = cirTreeOffset;
    this.cirTreeLength = cirTreeLength;
    this.isSummary = isSummary;
}

BED_COLOR_REGEXP = new RegExp("^[0-9]+,[0-9]+,[0-9]+");

BigWigView.prototype.readWigData = function(chrName, min, max, callback) {
    var chr = this.bwg.chromsToIDs[chrName];
    if (chr === undefined) {
        // Not an error because some .bwgs won't have data for all chromosomes.

        // dlog("Couldn't find chr " + chrName);
        // dlog('Chroms=' + miniJSONify(this.bwg.chromsToIDs));
        return callback([]);
    } else {
        this.readWigDataById(chr, min, max, callback);
    }
}

BigWigView.prototype.readWigDataById = function(chr, min, max, callback) {
    var thisB = this;
    if (!this.cirHeader) {
        // dlog('No CIR yet, fetching');
        this.bwg.data.slice(this.cirTreeOffset, 48).fetch(function(result) {
            thisB.cirHeader = result;
            var la = new Int32Array(thisB.cirHeader);
            thisB.cirBlockSize = la[1];
            thisB.readWigDataById(chr, min, max, callback);
        });
        return;
    }

    var blocksToFetch = [];
    var outstanding = 0;

    var beforeBWG = Date.now();

    var cirFobRecur = function(offset, level) {
        outstanding += offset.length;

        var maxCirBlockSpan = 4 +  (thisB.cirBlockSize * 32);   // Upper bound on size, based on a completely full leaf node.
        var spans;
        for (var i = 0; i < offset.length; ++i) {
            var blockSpan = new Range(offset[i], offset[i] + maxCirBlockSpan);
            spans = spans ? union(spans, blockSpan) : blockSpan;
        }
        
        var fetchRanges = spans.ranges();
        // console.log('fetchRanges: ' + fetchRanges);
        for (var r = 0; r < fetchRanges.length; ++r) {
            var fr = fetchRanges[r];
            cirFobStartFetch(offset, fr, level);
        }
    }

    var cirFobStartFetch = function(offset, fr, level, attempts) {
        var length = fr.max() - fr.min();
        // console.log('fetching ' + fr.min() + '-' + fr.max() + ' (' + (fr.max() - fr.min()) + ')');
        thisB.bwg.data.slice(fr.min(), fr.max() - fr.min()).fetch(function(resultBuffer) {
            for (var i = 0; i < offset.length; ++i) {
                if (fr.contains(offset[i])) {
                    cirFobRecur2(resultBuffer, offset[i] - fr.min(), level);
                    --outstanding;
                    if (outstanding == 0) {
                        cirCompleted();
                    }
                }
            }
        });
    }

    var cirFobRecur2 = function(cirBlockData, offset, level) {
        var ba = new Int8Array(cirBlockData);
        var sa = new Int16Array(cirBlockData);
        var la = new Int32Array(cirBlockData);

        var isLeaf = ba[offset];
        var cnt = sa[offset/2 + 1];
        // dlog('cir level=' + level + '; cnt=' + cnt);
        offset += 4;

        if (isLeaf != 0) {
            for (var i = 0; i < cnt; ++i) {
                var lo = offset/4;
                var startChrom = la[lo];
                var startBase = la[lo + 1];
                var endChrom = la[lo + 2];
                var endBase = la[lo + 3];
                var blockOffset = (la[lo + 4]<<32) | (la[lo + 5]);
                var blockSize = (la[lo + 6]<<32) | (la[lo + 7]);
                if (((chr < 0 || startChrom < chr) || (startChrom == chr && startBase <= max)) &&
                    ((chr < 0 || endChrom   > chr) || (endChrom == chr && endBase >= min)))
                {
                    // console.log('Got an interesting block: startChrom=' + startChrom + '; startBase=' + startBase + '; endChrom=' + endChrom + ' ;endBase=' + endBase + '; offset=' + blockOffset + '; size=' + blockSize);
                    blocksToFetch.push({offset: blockOffset, size: blockSize});
                }
                offset += 32;
            }
        } else {
            var recurOffsets = [];
            for (var i = 0; i < cnt; ++i) {
                var lo = offset/4;
                var startChrom = la[lo];
                var startBase = la[lo + 1];
                var endChrom = la[lo + 2];
                var endBase = la[lo + 3];
                var blockOffset = (la[lo + 4]<<32) | (la[lo + 5]);
                if ((chr < 0 || startChrom < chr || (startChrom == chr && startBase <= max)) &&
                    (chr < 0 || endChrom   > chr || (endChrom == chr && endBase >= min)))
                {
                    recurOffsets.push(blockOffset);
                    // console.log('interesting inner: startChrom=' + startChrom + '; endChrom=' + endChrom);
                }
                offset += 24;
            }
            if (recurOffsets.length > 0) {
                cirFobRecur(recurOffsets, level + 1);
            }
        }
    };
    

    var cirCompleted = function() {
        blocksToFetch.sort(function(b0, b1) {
            return (b0.offset|0) - (b1.offset|0);
        });

        if (blocksToFetch.length == 0) {
            callback([]);
        } else {
            var features = [];
            var createFeature = function(chr, fmin, fmax, opts) {
                // dlog('createFeature(' + fmin +', ' + fmax + ')');

                if (!opts) {
                    opts = {};
                }
            
                var f = new DASFeature();
                f._chromId = chr;
                f.segment = thisB.bwg.idsToChroms[chr];
                f.min = fmin;
                f.max = fmax;
                f.type = 'bigwig';
                
                for (k in opts) {
                    f[k] = opts[k];
                }
                
                features.push(f);
            };
            var maybeCreateFeature = function(chromId, fmin, fmax, opts) {
                if ((chr < 0 || chromId == chr) && fmin <= max && fmax >= min) {
                    createFeature(chromId, fmin, fmax, opts);
                }
            };
            var tramp = function() {
                if (blocksToFetch.length == 0) {
                    var afterBWG = Date.now();
                    // dlog('BWG fetch took ' + (afterBWG - beforeBWG) + 'ms');
                    callback(features);
                    return;  // just in case...
                } else {
                    var block = blocksToFetch[0];
                    if (block.data) {
                        var ba = new Uint8Array(block.data);

                        if (thisB.isSummary) {
                            var sa = new Int16Array(block.data);
                            var la = new Int32Array(block.data);
                            var fa = new Float32Array(block.data);

                            var itemCount = block.data.byteLength/32;
                            for (var i = 0; i < itemCount; ++i) {
                                var chromId =   la[(i*8)];
                                var start =     la[(i*8)+1];
                                var end =       la[(i*8)+2];
                                var validCnt =  la[(i*8)+3];
                                var minVal    = fa[(i*8)+4];
                                var maxVal    = fa[(i*8)+5];
                                var sumData   = fa[(i*8)+6];
                                var sumSqData = fa[(i*8)+7];
                                
                                if (chr < 0 || chromId == chr) {
                                    var summaryOpts = {type: 'bigwig', score: sumData/validCnt, maxScore: maxVal};
                                    if (thisB.bwg.type == 'bigbed') {
                                        summaryOpts.type = 'density';
                                    }
                                    maybeCreateFeature(chromId, start + 1, end, summaryOpts);
                                }
                            }
                        } else if (thisB.bwg.type == 'bigwig') {
                            var sa = new Int16Array(block.data);
                            var la = new Int32Array(block.data);
                            var fa = new Float32Array(block.data);

                            var chromId = la[0];
                            var blockStart = la[1];
                            var blockEnd = la[2];
                            var itemStep = la[3];
                            var itemSpan = la[4];
                            var blockType = ba[20];
                            var itemCount = sa[11];

                            // dlog('processing bigwig block, type=' + blockType + '; count=' + itemCount);
                            
                            if (blockType == BIG_WIG_TYPE_FSTEP) {
                                for (var i = 0; i < itemCount; ++i) {
                                    var score = fa[i + 6];
                                    maybeCreateFeature(chromId, blockStart + (i*itemStep) + 1, blockStart + (i*itemStep) + itemSpan, {score: score});
                                }
                            } else if (blockType == BIG_WIG_TYPE_VSTEP) {
                                for (var i = 0; i < itemCount; ++i) {
                                    var start = la[(i*2) + 6];
                                    var score = fa[(i*2) + 7];
                                    maybeCreateFeature(chromId, start + 1, start + itemSpan, {score: score});
                                }
                            } else if (blockType == BIG_WIG_TYPE_GRAPH) {
                                for (var i = 0; i < itemCount; ++i) {
                                    var start = la[(i*3) + 6];
                                    var end   = la[(i*3) + 7];
                                    var score = fa[(i*3) + 8];
                                    if (start > end) {
                                        start = end;
                                    }
                                    maybeCreateFeature(chromId, start + 1, end, {score: score});
                                }
                            } else {
                                dlog('Currently not handling bwgType=' + blockType);
                            }
                        } else if (thisB.bwg.type == 'bigbed') {
                            var offset = 0;
                            while (offset < ba.length) {
                                var chromId = (ba[offset+3]<<24) | (ba[offset+2]<<16) | (ba[offset+1]<<8) | (ba[offset+0]);
                                var start = (ba[offset+7]<<24) | (ba[offset+6]<<16) | (ba[offset+5]<<8) | (ba[offset+4]);
                                var end = (ba[offset+11]<<24) | (ba[offset+10]<<16) | (ba[offset+9]<<8) | (ba[offset+8]);
                                offset += 12;
                                var rest = '';
                                while (true) {
                                    var ch = ba[offset++];
                                    if (ch != 0) {
                                        rest += String.fromCharCode(ch);
                                    } else {
                                        break;
                                    }
                                }

                                var featureOpts = {};
                                
                                var bedColumns = rest.split('\t');
                                if (bedColumns.length > 0) {
                                    featureOpts.label = bedColumns[0];
                                }
                                if (bedColumns.length > 1) {
                                    featureOpts.score = stringToInt(bedColumns[1]);
                                }
                                if (bedColumns.length > 2) {
                                    featureOpts.orientation = bedColumns[2];
                                }
                                if (bedColumns.length > 5) {
                                    var color = bedColumns[5];
                                    if (BED_COLOR_REGEXP.test(color)) {
                                        featureOpts.override_color = 'rgb(' + color + ')';
                                    }
                                }

                                if (bedColumns.length < 9) {
                                    if (chromId == chr) {
                                        maybeCreateFeature(chromId, start + 1, end, featureOpts);
                                    }
                                } else if (chromId == chr && start <= max && end >= min) {
                                    // Complex-BED?
                                    // FIXME this is currently a bit of a hack to do Clever Things with ensGene.bb

                                    var thickStart = bedColumns[3]|0;
                                    var thickEnd   = bedColumns[4]|0;
                                    var blockCount = bedColumns[6]|0;
                                    var blockSizes = bedColumns[7].split(',');
                                    var blockStarts = bedColumns[8].split(',');
                                    
                                    featureOpts.type = 'bb-transcript'
                                    var grp = new DASGroup();
                                    grp.id = bedColumns[0];
                                    grp.type = 'bb-transcript'
                                    grp.notes = [];
                                    featureOpts.groups = [grp];

                                    if (bedColumns.length > 10) {
                                        var geneId = bedColumns[9];
                                        var geneName = bedColumns[10];
                                        var gg = new DASGroup();
                                        gg.id = geneId;
                                        gg.label = geneName;
                                        gg.type = 'gene';
                                        featureOpts.groups.push(gg);
                                    }

                                    var spans = null;
                                    for (var b = 0; b < blockCount; ++b) {
                                        var bmin = (blockStarts[b]|0) + start;
                                        var bmax = bmin + (blockSizes[b]|0);
                                        var span = new Range(bmin, bmax);
                                        if (spans) {
                                            spans = union(spans, span);
                                        } else {
                                            spans = span;
                                        }
                                    }
                                    
                                    var tsList = spans.ranges();
                                    for (var s = 0; s < tsList.length; ++s) {
                                        var ts = tsList[s];
                                        createFeature(chromId, ts.min() + 1, ts.max(), featureOpts);
                                    }

                                    if (thickEnd > thickStart) {
                                        var tl = intersection(spans, new Range(thickStart, thickEnd));
                                        if (tl) {
                                            featureOpts.type = 'bb-translation';
                                            var tlList = tl.ranges();
                                            for (var s = 0; s < tlList.length; ++s) {
                                                var ts = tlList[s];
                                                createFeature(chromId, ts.min() + 1, ts.max(), featureOpts);
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            dlog("Don't know what to do with " + thisB.bwg.type);
                        }
                        blocksToFetch.splice(0, 1);
                        tramp();
                    } else {
                        var fetchStart = block.offset;
                        var fetchSize = block.size;
                        var bi = 1;
                        while (bi < blocksToFetch.length && blocksToFetch[bi].offset == (fetchStart + fetchSize)) {
                            fetchSize += blocksToFetch[bi].size;
                            ++bi;
                        }

                        thisB.bwg.data.slice(fetchStart, fetchSize).fetch(function(result) {
                            var offset = 0;
                            var bi = 0;
                            while (offset < fetchSize) {
                                var fb = blocksToFetch[bi];
                            
                                var data;
                                if (thisB.bwg.uncompressBufSize > 0) {
                                    // var beforeInf = Date.now();
                                    data = jszlib_inflate_buffer(result, offset + 2, fb.size - 2);
                                    // var afterInf = Date.now();
                                    // dlog('inflate: ' + (afterInf - beforeInf) + 'ms');
                                } else {
                                    var tmp = new Uint8Array(fb.size);    // FIXME is this really the best we can do?
                                    arrayCopy(new Uint8Array(result, offset, fb.size), 0, tmp, 0, fb.size);
                                    data = tmp.buffer;
                                }
                                fb.data = data;
                                
                                offset += fb.size;
                                ++bi;
                            }
                            tramp();
                        });
                    }
                }
            }
            tramp();
        }
    }

    cirFobRecur([thisB.cirTreeOffset + 48], 1);
}

//
// nasty cut/paste, should roll back in!
//

BigWigView.prototype.getFirstAdjacent = function(chrName, pos, dir, callback) {
    var chr = this.bwg.chromsToIDs[chrName];
    if (chr === undefined) {
        // Not an error because some .bwgs won't have data for all chromosomes.

        // dlog("Couldn't find chr " + chrName);
        // dlog('Chroms=' + miniJSONify(this.bwg.chromsToIDs));
        return callback([]);
    } else {
        this.getFirstAdjacentById(chr, pos, dir, callback);
    }
}

BigWigView.prototype.getFirstAdjacentById = function(chr, pos, dir, callback) {
    var thisB = this;
    if (!this.cirHeader) {
        // dlog('No CIR yet, fetching');
        this.bwg.data.slice(this.cirTreeOffset, 48).fetch(function(result) {
            thisB.cirHeader = result;
            var la = new Int32Array(thisB.cirHeader);
            thisB.cirBlockSize = la[1];
            thisB.readWigDataById(chr, min, max, callback);
        });
        return;
    }

    var blockToFetch = null;
    var bestBlockChr = -1;
    var bestBlockOffset = -1;

    var outstanding = 0;

    var beforeBWG = Date.now();

    var cirFobRecur = function(offset, level) {
        outstanding += offset.length;

        var maxCirBlockSpan = 4 +  (thisB.cirBlockSize * 32);   // Upper bound on size, based on a completely full leaf node.
        var spans;
        for (var i = 0; i < offset.length; ++i) {
            var blockSpan = new Range(offset[i], Math.min(offset[i] + maxCirBlockSpan, thisB.cirTreeOffset + thisB.cirTreeLength));
            spans = spans ? union(spans, blockSpan) : blockSpan;
        }
        
        var fetchRanges = spans.ranges();
        // dlog('fetchRanges: ' + fetchRanges);
        for (var r = 0; r < fetchRanges.length; ++r) {
            var fr = fetchRanges[r];
            cirFobStartFetch(offset, fr, level);
        }
    }

    var cirFobStartFetch = function(offset, fr, level, attempts) {
        var length = fr.max() - fr.min();
        // dlog('fetching ' + fr.min() + '-' + fr.max() + ' (' + (fr.max() - fr.min()) + ')');
        thisB.bwg.data.slice(fr.min(), fr.max() - fr.min()).fetch(function(result) {
            var resultBuffer = result;

// This is now handled in URLFetchable instead.
//
//            if (resultBuffer.byteLength != length) {           
//                dlog("Didn't get expected size: " + resultBuffer.byteLength + " != " + length);
//                return cirFobStartFetch(offset, fr, level, attempts + 1);
//            }


            for (var i = 0; i < offset.length; ++i) {
                if (fr.contains(offset[i])) {
                    cirFobRecur2(resultBuffer, offset[i] - fr.min(), level);
                    --outstanding;
                    if (outstanding == 0) {
                        cirCompleted();
                    }
                }
            }
        });
    }

    var cirFobRecur2 = function(cirBlockData, offset, level) {
        var ba = new Int8Array(cirBlockData);
        var sa = new Int16Array(cirBlockData);
        var la = new Int32Array(cirBlockData);

        var isLeaf = ba[offset];
        var cnt = sa[offset/2 + 1];
        // dlog('cir level=' + level + '; cnt=' + cnt);
        offset += 4;

        if (isLeaf != 0) {
            for (var i = 0; i < cnt; ++i) {
                var lo = offset/4;
                var startChrom = la[lo];
                var startBase = la[lo + 1];
                var endChrom = la[lo + 2];
                var endBase = la[lo + 3];
                var blockOffset = (la[lo + 4]<<32) | (la[lo + 5]);
                var blockSize = (la[lo + 6]<<32) | (la[lo + 7]);
                // dlog('startChrom=' + startChrom);
                if ((dir < 0 && ((startChrom < chr || (startChrom == chr && startBase <= pos)))) ||
                    (dir > 0 && ((endChrom > chr || (endChrom == chr && endBase >= pos)))))
                {
                    // dlog('Got an interesting block: startBase=' + startChrom + ':' + startBase + '; endBase=' + endChrom + ':' + endBase + '; offset=' + blockOffset + '; size=' + blockSize);
                    if (/_random/.exec(thisB.bwg.idsToChroms[startChrom])) {
                        // dlog('skipping random: ' + thisB.bwg.idsToChroms[startChrom]);
                    } else if (blockToFetch == null || ((dir < 0) && (endChrom > bestBlockChr || (endChrom == bestBlockChr && endBase > bestBlockOffset)) ||
                                                 (dir > 0) && (startChrom < bestBlockChr || (startChrom == bestBlockChr && startBase < bestBlockOffset))))
                    {
                        //                        dlog('best is: startBase=' + startChrom + ':' + startBase + '; endBase=' + endChrom + ':' + endBase + '; offset=' + blockOffset + '; size=' + blockSize);
                        blockToFetch = {offset: blockOffset, size: blockSize};
                        bestBlockOffset = (dir < 0) ? endBase : startBase;
                        bestBlockChr = (dir < 0) ? endChrom : startChrom;
                    }
                }
                offset += 32;
            }
        } else {
            var bestRecur = -1;
            var bestPos = -1;
            var bestChr = -1;
            for (var i = 0; i < cnt; ++i) {
                var lo = offset/4;
                var startChrom = la[lo];
                var startBase = la[lo + 1];
                var endChrom = la[lo + 2];
                var endBase = la[lo + 3];
                var blockOffset = (la[lo + 4]<<32) | (la[lo + 5]);
                // dlog('startChrom=' + startChrom);
                if ((dir < 0 && ((startChrom < chr || (startChrom == chr && startBase <= pos)) &&
                                 (endChrom   >= chr))) ||
                     (dir > 0 && ((endChrom > chr || (endChrom == chr && endBase >= pos)) &&
                                  (startChrom <= chr))))
                {
                    // dlog('Got an interesting block: startBase=' + startChrom + ':' + startBase + '; endBase=' + endChrom + ':' + endBase + '; offset=' + blockOffset + '; size=' + blockSize);
                    if (bestRecur < 0 || endBase > bestPos) {
                        bestRecur = blockOffset;
                        bestPos = (dir < 0) ? endBase : startBase;
                        bestChr = (dir < 0) ? endChrom : startChrom;
                    }
                }
                offset += 24;
            }
            if (bestRecur >= 0) {
                cirFobRecur([bestRecur], level + 1);
            }
        }
    };
    

    var cirCompleted = function() {
        if (blockToFetch == null) {
            return dlog('got nothing');
        } 
        var blocksToFetch = [blockToFetch];

        blocksToFetch.sort(function(b0, b1) {
            return (b0.offset|0) - (b1.offset|0);
        });

        if (blocksToFetch.length == 0) {
            callback([]);
        } else {
            var bestFeature = null;
            var bestChr = -1;
            var bestPos = -1;
            var createFeature = function(chrx, fmin, fmax, opts) {
//                dlog('createFeature(' + fmin +', ' + fmax + ')');

                if (!opts) {
                    opts = {};
                }
            
                var f = new DASFeature();
                f.segment = thisB.bwg.idsToChroms[chrx];
                f.min = fmin;
                f.max = fmax;
                f.type = 'bigwig';
                
                for (k in opts) {
                    f[k] = opts[k];
                }
                
                if (bestFeature == null || ((dir < 0) && (chrx > bestChr || fmax > bestPos)) || ((dir > 0) && (chrx < bestChr || fmin < bestPos))) {
                    bestFeature = f;
                    bestPos = (dir < 0) ? fmax : fmin;
                    bestChr = chrx;
                }
            };
            var maybeCreateFeature = function(chrx, fmin, fmax, opts) {
//                dlog('maybeCreateFeature(' + thisB.bwg.idsToChroms[chrx] + ',' + fmin + ',' + fmax + ')');
                if ((dir < 0 && (chrx < chr || fmax < pos)) || (dir > 0 && (chrx > chr || fmin > pos))) {
                //                if (fmin <= max && fmax >= min) {
                    createFeature(chrx, fmin, fmax, opts);
                    //}
                }
            };
            var tramp = function() {
                if (blocksToFetch.length == 0) {
                    var afterBWG = Date.now();
                    // dlog('BWG fetch took ' + (afterBWG - beforeBWG) + 'ms');
                    callback([bestFeature]);
                    return;  // just in case...
                } else {
                    var block = blocksToFetch[0];
                    if (block.data) {
                        var ba = new Uint8Array(block.data);

                        if (thisB.isSummary) {
                            var sa = new Int16Array(block.data);
                            var la = new Int32Array(block.data);
                            var fa = new Float32Array(block.data);

                            var itemCount = block.data.byteLength/32;
                            for (var i = 0; i < itemCount; ++i) {
                                var chromId =   la[(i*8)];
                                var start =     la[(i*8)+1];
                                var end =       la[(i*8)+2];
                                var validCnt =  la[(i*8)+3];
                                var minVal    = fa[(i*8)+4];
                                var maxVal    = fa[(i*8)+5];
                                var sumData   = fa[(i*8)+6];
                                var sumSqData = fa[(i*8)+7];
                                
                                var summaryOpts = {type: 'bigwig', score: sumData/validCnt};
                                if (thisB.bwg.type == 'bigbed') {
                                    summaryOpts.type = 'density';
                                }
                                maybeCreateFeature(chromId, start + 1, end, summaryOpts);
                            }
                        } else if (thisB.bwg.type == 'bigwig') {
                            var sa = new Int16Array(block.data);
                            var la = new Int32Array(block.data);
                            var fa = new Float32Array(block.data);

                            var chromId = la[0];
                            var blockStart = la[1];
                            var blockEnd = la[2];
                            var itemStep = la[3];
                            var itemSpan = la[4];
                            var blockType = ba[20];
                            var itemCount = sa[11];

                            // dlog('processing bigwig block, type=' + blockType + '; count=' + itemCount);
                            
                            if (blockType == BIG_WIG_TYPE_FSTEP) {
                                for (var i = 0; i < itemCount; ++i) {
                                    var score = fa[i + 6];
                                    maybeCreateFeature(chromId, blockStart + (i*itemStep) + 1, blockStart + (i*itemStep) + itemSpan, {score: score});
                                }
                            } else if (blockType == BIG_WIG_TYPE_VSTEP) {
                                for (var i = 0; i < itemCount; ++i) {
                                    var start = la[(i*2) + 6];
                                    var score = fa[(i*2) + 7];
                                    maybeCreateFeature(start + 1, start + itemSpan, {score: score});
                                }
                            } else if (blockType == BIG_WIG_TYPE_GRAPH) {
                                for (var i = 0; i < itemCount; ++i) {
                                    var start = la[(i*3) + 6] + 1;
                                    var end   = la[(i*3) + 7];
                                    var score = fa[(i*3) + 8];
                                    if (start > end) {
                                        start = end;
                                    }
                                    maybeCreateFeature(start + 1, end, {score: score});
                                }
                            } else {
                                dlog('Currently not handling bwgType=' + blockType);
                            }
                        } else if (thisB.bwg.type == 'bigbed') {
                            var offset = 0;
                            while (offset < ba.length) {
                                var chromId = (ba[offset+3]<<24) | (ba[offset+2]<<16) | (ba[offset+1]<<8) | (ba[offset+0]);
                                var start = (ba[offset+7]<<24) | (ba[offset+6]<<16) | (ba[offset+5]<<8) | (ba[offset+4]);
                                var end = (ba[offset+11]<<24) | (ba[offset+10]<<16) | (ba[offset+9]<<8) | (ba[offset+8]);
                                offset += 12;
                                var rest = '';
                                while (true) {
                                    var ch = ba[offset++];
                                    if (ch != 0) {
                                        rest += String.fromCharCode(ch);
                                    } else {
                                        break;
                                    }
                                }

                                var featureOpts = {};
                                
                                var bedColumns = rest.split('\t');
                                if (bedColumns.length > 0) {
                                    featureOpts.label = bedColumns[0];
                                }
                                if (bedColumns.length > 1) {
                                    featureOpts.score = 100; /* bedColumns[1]; */
                                }
                                if (bedColumns.length > 2) {
                                    featureOpts.orientation = bedColumns[2];
                                }

                                maybeCreateFeature(chromId, start + 1, end, featureOpts);
                            }
                        } else {
                            dlog("Don't know what to do with " + thisB.bwg.type);
                        }
                        blocksToFetch.splice(0, 1);
                        tramp();
                    } else {
                        var fetchStart = block.offset;
                        var fetchSize = block.size;
                        var bi = 1;
                        while (bi < blocksToFetch.length && blocksToFetch[bi].offset == (fetchStart + fetchSize)) {
                            fetchSize += blocksToFetch[bi].size;
                            ++bi;
                        }

                        thisB.bwg.data.slice(fetchStart, fetchSize).fetch(function(result) {
                            var offset = 0;
                            var bi = 0;
                            while (offset < fetchSize) {
                                var fb = blocksToFetch[bi];
                            
                                var data;
                                if (thisB.bwg.uncompressBufSize > 0) {
                                    // var beforeInf = Date.now()
                                    data = jszlib_inflate_buffer(result, offset + 2, fb.size - 2);
                                    // var afterInf = Date.now();
                                    // dlog('inflate: ' + (afterInf - beforeInf) + 'ms');
                                } else {
                                    var tmp = new Uint8Array(fb.size);    // FIXME is this really the best we can do?
                                    arrayCopy(new Uint8Array(result, offset, fb.size), 0, tmp, 0, fb.size);
                                    data = tmp.buffer;
                                }
                                fb.data = data;
                                
                                offset += fb.size;
                                ++bi;
                            }
                            tramp();
                        });
                    }
                }
            }
            tramp();
        }
    }

    cirFobRecur([thisB.cirTreeOffset + 48], 1);
}

//
// end cut/paste
//






BigWig.prototype.readWigData = function(chrName, min, max, callback) {
    this.getUnzoomedView().readWigData(chrName, min, max, callback);
}

BigWig.prototype.getUnzoomedView = function() {
    if (!this.unzoomedView) {
        var cirLen = 4000;
        var nzl = this.zoomLevels[0];
        if (nzl) {
            cirLen = this.zoomLevels[0].dataOffset - this.unzoomedIndexOffset;
        }
        this.unzoomedView = new BigWigView(this, this.unzoomedIndexOffset, cirLen, false);
    }
    return this.unzoomedView;
}

BigWig.prototype.getZoomedView = function(z) {
    var zh = this.zoomLevels[z];
    if (!zh.view) {
        zh.view = new BigWigView(this, zh.indexOffset, /* this.zoomLevels[z + 1].dataOffset - zh.indexOffset */ 4000, true);
    }
    return zh.view;
}


function makeBwgFromURL(url, callback, creds) {
    makeBwg(new URLFetchable(url, {credentials: creds}), callback, url);
}

function makeBwgFromFile(file, callback) {
    makeBwg(new BlobFetchable(file), callback, 'file');
}

function makeBwg(data, callback, name) {
    var bwg = new BigWig();
    bwg.data = data;
    bwg.name = name;
    bwg.data.slice(0, 512).fetch(function(result) {
        if (!result) {
            return callback(null, "Couldn't fetch file");
        }

        var header = result;
        var sa = new Int16Array(header);
        var la = new Int32Array(header);
        if (la[0] == BIG_WIG_MAGIC) {
            bwg.type = 'bigwig';
        } else if (la[0] == BIG_BED_MAGIC) {
            bwg.type = 'bigbed';
        } else {
            callback(null, "Not a supported format");
        }
        // console.log('magic okay');

        bwg.version = sa[2];             // 4
        bwg.numZoomLevels = sa[3];       // 6
        bwg.chromTreeOffset = (la[2] << 32) | (la[3]);     // 8
        bwg.unzoomedDataOffset = (la[4] << 32) | (la[5]);  // 16
        bwg.unzoomedIndexOffset = (la[6] << 32) | (la[7]); // 24
        bwg.fieldCount = sa[16];         // 32
        bwg.definedFieldCount = sa[17];  // 34
        bwg.asOffset = (la[9] << 32) | (la[10]);    // 36 (unaligned longlong)
        bwg.totalSummaryOffset = (la[11] << 32) | (la[12]);    // 44 (unaligned longlong)
        bwg.uncompressBufSize = la[13];  // 52
        
        // console.log('bwgVersion: ' + bwg.version);
        // dlog('bigType: ' + bwg.type);
        // dlog('chromTree at: ' + bwg.chromTreeOffset);
        // dlog('uncompress: ' + bwg.uncompressBufSize);
        // dlog('data at: ' + bwg.unzoomedDataOffset);
        // dlog('index at: ' + bwg.unzoomedIndexOffset);
        // dlog('field count: ' + bwg.fieldCount);
        // dlog('defined count: ' + bwg.definedFieldCount);

        bwg.zoomLevels = [];
        for (var zl = 0; zl < bwg.numZoomLevels; ++zl) {
            var zlReduction = la[zl*6 + 16]
            var zlData = (la[zl*6 + 18]<<32)|(la[zl*6 + 19]);
            var zlIndex = (la[zl*6 + 20]<<32)|(la[zl*6 + 21]);
//          dlog('zoom(' + zl + '): reduction=' + zlReduction + '; data=' + zlData + '; index=' + zlIndex);
            bwg.zoomLevels.push({reduction: zlReduction, dataOffset: zlData, indexOffset: zlIndex});
        }

        bwg.readChromTree(function() {
            return callback(bwg);
        });
    });
}


BigWig.prototype._tsFetch = function(zoom, chr, min, max, callback) {
    var bwg = this;
    // console.log('tsFetch: ' + zoom + ', ' + chr + ', ' + min + ', ' + max);
    if (zoom >= this.zoomLevels.length - 1) {
        if (!this.topLevelReductionCache) {
            this.getZoomedView(this.zoomLevels.length - 1).readWigDataById(-1, 0, 300000000, function(feats) {
                bwg.topLevelReductionCache = feats;
                return bwg._tsFetch(zoom, chr, min, max, callback);
            });
        } else {
            var f = [];
            var c = this.topLevelReductionCache;
            for (var fi = 0; fi < c.length; ++fi) {
                if (c[fi]._chromId == chr) {
                    f.push(c[fi]);
                }
            }
            return callback(f);
        }
    } else {
        return this.getZoomedView(zoom).readWigDataById(chr, min, max, callback);
    }
}

BigWig.prototype.thresholdSearch = function(chrName, referencePoint, dir, threshold, callback) {
    // console.log('ref=' + referencePoint + '; dir=' + dir);

    dir = (dir<0) ? -1 : 1;
    var bwg = this;
    var initialChr = this.chromsToIDs[chrName];
    var candidates = [{chrOrd: 0, chr: initialChr, zoom: bwg.zoomLevels.length - 4, min: 0, max: 300000000, fromRef: true}]
    for (var i = 1; i <= this.maxID + 1; ++i) {
        var chrId = (initialChr + (dir*i)) % (this.maxID + 1);
        if (chrId < 0) 
            chrId += (this.maxID + 1);
        candidates.push({chrOrd: i, chr: chrId, zoom: bwg.zoomLevels.length - 1, min: 0, max: 300000000})
    }
       
    function fbThresholdSearchRecur() {
	if (candidates.length == 0) {
	    return callback(null);
	}
	candidates.sort(function(c1, c2) {
	    var d = c1.zoom - c2.zoom;
	    if (d != 0)
		return d;

            d = c1.chrOrd - c2.chrOrd;
            if (d != 0)
                return d;
	    else
		return c1.min - c2.min * dir;
	});

	var candidate = candidates.splice(0, 1)[0];
        // console.log('trying ' + miniJSONify(candidate));

        bwg._tsFetch(candidate.zoom, candidate.chr, candidate.min, candidate.max, function(feats) {
            var rp = dir > 0 ? 0 : 300000000;
            if (candidate.fromRef)
                rp = referencePoint;
            
            for (var fi = 0; fi < feats.length; ++fi) {
	        var f = feats[fi];
                

                if (dir > 0) {
	            if (f.maxScore > threshold) {
		        if (candidate.zoom == 0) {
		            if (f.min > rp)
			        return callback(f);
		        } else if (f.max > rp) {
		            candidates.push({chr: candidate.chr, chrOrd: candidate.chrOrd, zoom: Math.max(0, candidate.zoom - 2), min: f.min, max: f.max, fromRef: candidate.fromRef});
		        }
	            }
                } else {
                    if (f.maxScore > threshold) {
		        if (candidate.zoom == 0) {
		            if (f.max < rp)
			        return callback(f);
		        } else if (f.min < rp) {
		            candidates.push({chr: candidate.chr, chrOrd: candidate.chrOrd, zoom: Math.max(0, candidate.zoom - 2), min: f.min, max: f.max, fromRef: candidate.fromRef});
		        }
	            }
                }
	    }
            fbThresholdSearchRecur();
        });
    }
    
    fbThresholdSearchRecur();
}
/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2011
//
// bin.js general binary data support
//

function BlobFetchable(b) {
    this.blob = b;
}

BlobFetchable.prototype.slice = function(start, length) {
    var b;

    if (this.blob.slice) {
        if (length) {
            b = this.blob.slice(start, start + length);
        } else {
            b = this.blob.slice(start);
        }
    } else {
        if (length) {
            b = this.blob.webkitSlice(start, start + length);
        } else {
            b = this.blob.webkitSlice(start);
        }
    }
    return new BlobFetchable(b);
}

BlobFetchable.prototype.fetch = function(callback) {
    var reader = new FileReader();
    reader.onloadend = function(ev) {
        callback(bstringToBuffer(reader.result));
    };
    reader.readAsBinaryString(this.blob);
}

function URLFetchable(url, start, end, opts) {
    if (!opts) {
        if (typeof start === 'object') {
            opts = start;
            start = undefined;
        } else {
            opts = {};
        }
    }

    this.url = url;
    this.start = start || 0;
    if (end) {
        this.end = end;
    }
    this.opts = opts;
}

URLFetchable.prototype.slice = function(s, l) {
    var ns = this.start, ne = this.end;
    if (ns && s) {
        ns = ns + s;
    } else {
        ns = s || ns;
    }
    if (l && ns) {
        ne = ns + l - 1;
    } else {
        ne = ne || l - 1;
    }
    return new URLFetchable(this.url, ns, ne, this.opts);
}

var seed=0;
var isIOS = navigator.userAgent.indexOf('Safari') >= 0 && navigator.userAgent.indexOf('Chrome') < 0 ;

URLFetchable.prototype.fetch = function(callback, attempt, truncatedLength) {
    var thisB = this;

    attempt = attempt || 1;
    if (attempt > 3) {
        return callback(null);
    }

    var req = new XMLHttpRequest();
    var length;
    var url = this.url;
    if (isIOS) {
        // console.log('Safari hack');
        url = url + '?salt=' + (++seed);
    }
    req.open('GET', url, true);
    req.overrideMimeType('text/plain; charset=x-user-defined');
    if (this.end) {
        // console.log('req bytes=' + this.start + '-' + this.end);
        req.setRequestHeader('Range', 'bytes=' + this.start + '-' + this.end);
        length = this.end - this.start + 1;
    }
    req.responseType = 'arraybuffer';
    req.onreadystatechange = function() {
        if (req.readyState == 4) {
            if (req.status == 200 || req.status == 206) {
                if (req.response) {
                    var bl = req.response.byteLength;
                    // dlog('Got ' + bl + ' expected ' + length);
                    if (length && length != bl && (!truncatedLength || bl != truncatedLength)) {
                        return thisB.fetch(callback, attempt + 1, bl);
                    } else {
                        return callback(req.response);
                    }
                } else if (req.mozResponseArrayBuffer) {
                    return callback(req.mozResponseArrayBuffer);
                } else {
                    var r = req.responseText;
                    if (length && length != r.length && (!truncatedLength || r.length != truncatedLength)) {
                        return thisB.fetch(callback, attempt + 1, r.length);
                    } else {
                        return callback(bstringToBuffer(req.responseText));
                    }
                }
            } else {
                return thisB.fetch(callback, attempt + 1);
            }
        }
    };
    if (this.opts.credentials) {
        req.withCredentials = true;
    }
    req.send('');
}

function bstringToBuffer(result) {
    if (!result) {
        return null;
    }

//    var before = Date.now();
    var ba = new Uint8Array(result.length);
    for (var i = 0; i < ba.length; ++i) {
        ba[i] = result.charCodeAt(i);
    }
//    var after  = Date.now();
//    dlog('bb took ' + (after - before) + 'ms');
    return ba.buffer;
}

/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2011
//
// cbrowser.js: canvas browser container
//

var NS_SVG = 'http://www.w3.org/2000/svg';
var NS_HTML = 'http://www.w3.org/1999/xhtml';
var NS_XLINK = 'http://www.w3.org/1999/xlink';

function Region(chr, min, max) {
    this.min = min;
    this.max = max;
    this.chr = chr;
}

function Browser(opts) {
    if (!opts) {
        opts = {};
    }

    // custom code
    this.uiPrefix = 'http://localhost:8080/';
    // custom code
    this.sources = [];
    this.tiers = [];

    this.featureListeners = [];
    this.featureHoverListeners = [];
    this.viewListeners = [];
    this.regionSelectListeners = [];
    this.tierListeners = [];
    this.tierSelectionWrapListeners = [];

    this.cookieKey = 'browser';
    this.karyoEndpoint = new DASSource('http://www.derkholm.net:8080/das/hsa_54_36p/');
    this.registry = 'http://www.dasregistry.org/das/sources';
    this.coordSystem = {
        speciesName: 'Human',
        taxon: 9606,
        auth: 'NCBI',
        version: '36',
        ucscName: 'hg18'
    };
    this.chains = {};

    this.pageName = 'svgHolder'
    this.maxExtra = 2.5;
    this.minExtra = 0.5;
    this.zoomFactor = 1.0;
    this.zoomMin = 10.0;
    this.zoomMax;       // Allow configuration for compatibility, but otherwise clobber.
    this.origin = 0;
    this.targetQuantRes = 5.0;
    this.featurePanelWidth = 750;
    this.zoomBase = 100;
    this.zoomExpt = 30.0; // Back to being fixed....
    this.zoomSliderValue = 100;
    this.entryPoints = null;
    this.currentSeqMax = -1; // init once EPs are fetched.

    this.highlights = [];

    this.autoSizeTiers = false;
    this.guidelineStyle = 'foreground';
    this.guidelineSpacing = 75;
    this.fgGuide = null;
    this.positionFeedback = false;

    this.selectedTier = 1;

    this.placards = [];

    this.maxViewWidth = 500000;

    // Options.
    
    this.reverseScrolling = false;
    this.rulerLocation = 'center';

    // Visual config.

    // this.tierBackgroundColors = ["rgb(245,245,245)", "rgb(230,230,250)" /* 'white' */];
    this.tierBackgroundColors = ["rgb(245,245,245)", 'white'];
    this.minTierHeight = 25;

    this.browserLinks = {
        Ensembl: 'http://ncbi36.ensembl.org/Homo_sapiens/Location/View?r=${chr}:${start}-${end}',
        UCSC: 'http://genome.ucsc.edu/cgi-bin/hgTracks?db=hg18&position=chr${chr}:${start}-${end}'
    }

    // Registry

    this.availableSources = new Observed();
    this.defaultSources = [];
    this.mappableSources = {};

    this.hubs = [];
    this.hubObjects = [];

    for (var k in opts) {
        this[k] = opts[k];
    }

    var thisB = this;
    window.addEventListener('load', function(ev) {thisB.realInit();}, false);
}

Browser.prototype.realInit = function() {
    this.supportsBinary = true; /* (typeof Int8Array === 'function');*/ 
    
    this.defaultChr = this.chr;
    this.defaultStart = this.viewStart;
    this.defaultEnd = this.viewEnd;
    this.defaultSources = [];
    for (var i = 0; i < this.sources.length; ++i) {
        this.defaultSources.push(this.sources[i]);
    }

    if (this.restoreStatus) {
        this.restoreStatus();
    }

    var helpPopup;
    var thisB = this;
    this.browserHolderHolder = document.getElementById(this.pageName);
    this.browserHolder = makeElement('div', null, {tabIndex: -1}, {outline: 'none'});
    removeChildren(this.browserHolderHolder);
    this.browserHolderHolder.appendChild(this.browserHolder);
    this.svgHolder = makeElement('div', null, {}, {overflow: 'hidden', display: 'inline-block', width: '100%', fontSize: '10pt', outline: 'none'});

    this.initUI(this.browserHolder, this.svgHolder);

    this.tierHolder = makeElement('div', null, {}, {width: '100%', padding: '0px', margin: '0px', border: '0px', position: 'relative', outline: 'none'});
    this.svgHolder.appendChild(this.tierHolder);

    this.bhtmlRoot = makeElement('div');
    if (!this.disablePoweredBy) {
        this.bhtmlRoot.appendChild(makeElement('span', ['Powered by ', makeElement('a', 'Dalliance', {href: 'http://www.biodalliance.org/'}), ' ' + VERSION]));
    }
    this.svgHolder.appendChild(this.bhtmlRoot);

    //
    // Window resize support (should happen before first fetch so we know the actual size of the viewed area).
    //

    // this.resizeViewer(true);
    this.featurePanelWidth = this.tierHolder.getBoundingClientRect().width | 0;
    window.addEventListener('resize', function(ev) {
        thisB.resizeViewer();
    }, false);

    this.ruler = makeElement('div', null, null, {width: '1px', height: '2000px', backgroundColor: 'blue', position: 'absolute', zIndex: '900', top: '0px'});
    this.tierHolder.appendChild(this.ruler);

    // Dimension stuff

    this.scale = this.featurePanelWidth / (this.viewEnd - this.viewStart);
    // this.zoomExpt = 250 / Math.log(/* MAX_VIEW_SIZE */ 500000.0 / this.zoomBase);
    if (!this.zoomMax) {
        this.zoomMax = this.zoomExpt * Math.log(this.maxViewWidth / this.zoomBase);
    }
    this.zoomSliderValue = this.zoomExpt * Math.log((this.viewEnd - this.viewStart + 1) / this.zoomBase);

    // Event handlers
    this.tierHolder.addEventListener('mousewheel', function(ev) {
        if (!ev.wheelDeltaX) {
            return;
        }

        ev.stopPropagation(); ev.preventDefault();
        var delta = ev.wheelDeltaX/5;
        if (!thisB.reverseScrolling) {
            delta = -delta;
        }
        thisB.move(delta);
    }, false);
    this.tierHolder.addEventListener('MozMousePixelScroll', function(ev) {
        if (ev.axis == 1) {
            ev.stopPropagation(); ev.preventDefault();

            if (ev.detail != 0) {
                var delta = ev.detail/4;
                if (thisB.reverseScrolling) {
                    delta = -delta;
                }
                thisB.move(delta);
            }
        }
    }, false);


    /*
    this.tierHolder.addEventListener('touchstart', function(ev) {return thisB.touchStartHandler(ev)}, false);
    this.tierHolder.addEventListener('touchmove', function(ev) {return thisB.touchMoveHandler(ev)}, false);
    this.tierHolder.addEventListener('touchend', function(ev) {return thisB.touchEndHandler(ev)}, false);
    this.tierHolder.addEventListener('touchcancel', function(ev) {return thisB.touchCancelHandler(ev)}, false); 

    */

    var keyHandler = function(ev) {
        if (ev.keyCode == 13) { // enter
            var layoutsChanged = false;
            for (var ti = 0; ti < thisB.tiers.length; ++ti) {
                var t = thisB.tiers[ti];
                if (t.wantedLayoutHeight && t.wantedLayoutHeight != t.layoutHeight) {
                    t.layoutHeight = t.wantedLayoutHeight;
                    t.placard = null;
                    t.clipTier();
                    layoutsChanged = true;
                }
            }
            if (layoutsChanged) {
                thisB.arrangeTiers();
            }
        } else if (ev.keyCode == 32 || ev.charCode == 32) { // space
            // if (!thisB.snapZoomLockout) {
                if (!thisB.isSnapZooming) {
                    thisB.isSnapZooming = true;
                    var newZoom = thisB.savedZoom || 1.0;
                    thisB.savedZoom = thisB.zoomSliderValue;
                    thisB.zoomSliderValue = newZoom;
                    thisB.zoom(Math.exp((1.0 * newZoom) / thisB.zoomExpt));
                    // thisB.invalidateLayouts();
                    // thisB.zoomSlider.setColor('red');
                    // thisB.refresh();
                } else {
                    thisB.isSnapZooming = false;
                    var newZoom = thisB.savedZoom || 10.0;
                    thisB.savedZoom = thisB.zoomSliderValue;
                    thisB.zoomSliderValue = newZoom;
                    thisB.zoom(Math.exp((1.0 * newZoom) / thisB.zoomExpt));
                    // thisB.invalidateLayouts();
                    // thisB.zoomSlider.setColor('blue');
                    // thisB.refresh();
                }
                thisB.snapZoomLockout = true;
            // }
            ev.stopPropagation(); ev.preventDefault();      
        } else if (ev.keyCode == 39) { // right arrow
            ev.stopPropagation(); ev.preventDefault();
            if (ev.ctrlKey) {
                var fedge = 0;
                if(ev.shiftKey){
                    fedge = 1;
                }
                var pos=((thisB.viewStart + thisB.viewEnd + 1)/2)|0;
                thisB.tiers[thisB.selectedTier].findNextFeature(
                      thisB.chr,
                      pos,
                      -1,
                      fedge,
                      function(nxt) {
                          if (nxt) {
                              var nmin = nxt.min;
                              var nmax = nxt.max;
                              if (fedge) {
                                  if (nmax<pos-1) {
                                      nmax++;
                                      nmin=nmax;
                                  } else {
                                      nmax=nmin;
                                  }
                              }
                              var wid = thisB.viewEnd - thisB.viewStart + 1;
                              if(parseFloat(wid/2) == parseInt(wid/2)){wid--;}
                              var newStart = (nmin + nmax - wid)/2 + 1;
                              var newEnd = newStart + wid - 1;
                              var pos2=pos;
                              thisB.setLocation(nxt.segment, newStart, newEnd);
                          } else {
                              alert('no next feature');
                          }
                      });
            } else {
                thisB.move(ev.shiftKey ? 100 : 25);
            }
        } else if (ev.keyCode == 37) { // left arrow
            ev.stopPropagation(); ev.preventDefault();
            if (ev.ctrlKey) {
                var fedge = 0;
                if(ev.shiftKey){
                    fedge = 1;
                }
                var pos=((thisB.viewStart + thisB.viewEnd + 1)/2)|0;
                thisB.tiers[thisB.selectedTier].findNextFeature(
                      thisB.chr,
                      pos,
                      1,
                      fedge,
                      function(nxt) {
                          if (nxt) {
                              var nmin = nxt.min;
                              var nmax = nxt.max;
                              if (fedge) { 
                                  if (nmin>pos+1) {
                                      nmax=nmin;
                                  } else {
                                      nmax++;
                                      nmin=nmax
                                  }
                              }
                              var wid = thisB.viewEnd - thisB.viewStart + 1;
                              if(parseFloat(wid/2) == parseInt(wid/2)){wid--;}
                              var newStart = (nmin + nmax - wid)/2 + 1;
                              var newEnd = newStart + wid - 1;
                              var pos2=pos;
                              thisB.setLocation(nxt.segment, newStart, newEnd);
                          } else {
                              alert('no next feature'); // FIXME better reporting would be nice!
                          }
                      });
            } else {
                thisB.move(ev.shiftKey ? -100 : -25);
            }
        } else if (ev.keyCode == 38 || ev.keyCode == 87) { // up arrow | w
            ev.stopPropagation(); ev.preventDefault();

            if (ev.shiftKey) {
                var tt = thisB.tiers[thisB.selectedTier];
                var ch = tt.forceHeight || tt.subtiers[0].height;
                if (ch >= 40) {
                    tt.forceHeight = ch - 10;
                    tt.draw();
                }
            } else if (ev.ctrlKey) {
                var tt = thisB.tiers[thisB.selectedTier];
  
                if (tt.quantLeapThreshold) {
                    var th = tt.subtiers[0].height;
                    var tq = tt.subtiers[0].quant;
                    if (!tq)
                        return;

                    var qscale = (tq.max - tq.min) / th;
                    tt.quantLeapThreshold = tq.min + ((((tt.quantLeapThreshold - tq.min)/qscale)|0)+1)*qscale;
                    tt.draw();
                }                
            } else {
                if (thisB.selectedTier > 0) {
                    thisB.setSelectedTier(thisB.selectedTier - 1);
                } else {
                    thisB.notifyTierSelectionWrap(-1);
                }
            }
        } else if (ev.keyCode == 40 || ev.keyCode == 83) { // down arrow | s
            ev.stopPropagation(); ev.preventDefault();

            if (ev.shiftKey) {
                var tt = thisB.tiers[thisB.selectedTier];
                var ch = tt.forceHeight || tt.subtiers[0].height;
                tt.forceHeight = ch + 10;
                tt.draw();
            } else if (ev.ctrlKey) {
                var tt = thisB.tiers[thisB.selectedTier];

                if (tt.quantLeapThreshold) {
                    var th = tt.subtiers[0].height;
                    var tq = tt.subtiers[0].quant;
                    if (!tq)
                        return;

                    var qscale = (tq.max - tq.min) / th;
                    var it = ((tt.quantLeapThreshold - tq.min)/qscale)|0;
                    if (it > 1) {
                        tt.quantLeapThreshold = tq.min + (it-1)*qscale;
                        tt.draw();
                    }
                }
            } else {
                if (thisB.selectedTier < thisB.tiers.length -1) {
                    thisB.setSelectedTier(thisB.selectedTier + 1);
                }
            }
        } else if (ev.keyCode == 187 || ev.keyCode == 61) { // +
            ev.stopPropagation(); ev.preventDefault();
            thisB.zoomStep(-10);
        } else if (ev.keyCode == 189 || ev.keyCode == 173) { // -
            ev.stopPropagation(); ev.preventDefault();
            thisB.zoomStep(10);
        } else if (ev.keyCode == 72 || ev.keyCode == 104) { // h
            ev.stopPropagation(); ev.preventDefault();
            thisB.toggleHelpPopup(ev);
        } else if (ev.keyCode == 73 || ev.keyCode == 105) { // i
            ev.stopPropagation(); ev.preventDefault();
            var t = thisB.tiers[thisB.selectedTier];
            if (!t.infoVisible) {
                t.infoElement.style.display = 'block';
                t.updateHeight();
                t.infoVisible = true;
            } else {
                t.infoElement.style.display = 'none';
                t.updateHeight();
                t.infoVisible = false;
            }
        } else if (ev.keyCode == 84 || ev.keyCode == 116) { // t
            ev.stopPropagation(); ev.preventDefault();
            var bumpStatus;
            if( ev.shiftKey ){
                for (var ti = 0; ti < thisB.tiers.length; ++ti) {
                    var t = thisB.tiers[ti];
                    if (t.dasSource.collapseSuperGroups) {
                        if (bumpStatus === undefined) {
                            bumpStatus = !t.bumped;
                        }
                        t.bumped = bumpStatus;
                        t.layoutWasDone = false;
                        t.draw();
                        t.updateLabel();
                    }
                }
            } else {
                var t = thisB.tiers[thisB.selectedTier];
                if (t.dasSource.collapseSuperGroups) {
                    if (bumpStatus === undefined) {
                        bumpStatus = !t.bumped;
                    }
                    t.bumped = bumpStatus;
                    t.layoutWasDone = false;
                    t.draw();
                    t.updateLabel();
                }
            }
        } else {
            // console.log('key: ' + ev.keyCode + '; char: ' + ev.charCode);
        }
    };
    var keyUpHandler = function(ev) {
        thisB.snapZoomLockout = false;
    }

    this.browserHolder.addEventListener('focus', function(ev) {
        thisB.browserHolder.addEventListener('keydown', keyHandler, false);
    }, false);
    this.browserHolder.addEventListener('blur', function(ev) {
        thisB.browserHolder.removeEventListener('keydown', keyHandler, false);
    }, false);

    // Popup support (does this really belong here? FIXME)
    this.hPopupHolder = makeElement('div');
    this.hPopupHolder.style['font-family'] = 'helvetica';
    this.hPopupHolder.style['font-size'] = '12pt';
    this.hPopupHolder.classList.add('dalliance');
    document.body.appendChild(this.hPopupHolder);

    for (var t = 0; t < this.sources.length; ++t) {
        var source = this.sources[t];
        if (source.bwgURI && !this.supportsBinary) {
            if (!this.binaryWarningGiven) {
                this.popit({clientX: 300, clientY: 100}, 'Warning', makeElement('p', 'your browser does not support binary data formats, some track(s) not loaded.  We currently recommend Google Chrome 9 or later, or Firefox 4 or later.'));
                this.binaryWarningGiven = true;
            }
            continue;
        }
        this.makeTier(source);
    }
    thisB.arrangeTiers();
    thisB.refresh();
    thisB.setSelectedTier(1);

    thisB.positionRuler();


    for (var ti = 0; ti < this.tiers.length; ++ti) {
        var t = this.tiers[ti];
        if (t.sequenceSource) {
            t.sequenceSource.getSeqInfo(this.chr, function(si) {
                if (si) {
                    // console.log(si);
                    thisB.currentSeqMax = si.length;
                }
            });
            break;
        }
    }

    this.queryRegistry();
    for (var m in this.chains) {
        this.queryRegistry(m, true);
    }

    if (this.hubs) {
        for (var hi = 0; hi < this.hubs.length; ++hi) {
            connectTrackHub(this.hubs[hi], function(hub, err) {
                if (err) {
                    console.log(err);
                } else {
                    thisB.hubObjects.push(hub);
                }
            });
        }
    }
}

// 
// iOS touch support

Browser.prototype.touchStartHandler = function(ev)
{
    ev.stopPropagation(); ev.preventDefault();
    
    this.touchOriginX = ev.touches[0].pageX;
    if (ev.touches.length == 2) {
        var sep = Math.abs(ev.touches[0].pageX - ev.touches[1].pageX);
        this.zooming = true;
        this.zoomLastSep = this.zoomInitialSep = sep;
        this.zoomInitialScale = this.scale;
    }
}

Browser.prototype.touchMoveHandler = function(ev)
{
    ev.stopPropagation(); ev.preventDefault();
    
    if (ev.touches.length == 1) {
        var touchX = ev.touches[0].pageX;
        if (this.touchOriginX && touchX != this.touchOriginX) {
            this.move(touchX - this.touchOriginX);
        }
        this.touchOriginX = touchX;
    } else if (this.zooming && ev.touches.length == 2) {
        var sep = Math.abs(ev.touches[0].pageX - ev.touches[1].pageX);
        if (sep != this.zoomLastSep) {
            var cp = (ev.touches[0].pageX + ev.touches[1].pageX)/2;
            var scp = this.viewStart + (cp/this.scale)|0
            this.scale = this.zoomInitialScale * (sep/this.zoomInitialSep);
            this.viewStart = scp - (cp/this.scale)|0;
            for (var i = 0; i < this.tiers.length; ++i) {
	        this.tiers[i].draw();
            }
        }
        this.zoomLastSep = sep;
    }


}

Browser.prototype.touchEndHandler = function(ev)
{
    ev.stopPropagation(); ev.preventDefault();
}

Browser.prototype.touchCancelHandler = function(ev) {
}


Browser.prototype.makeTier = function(source) {
    try {
        this.realMakeTier(source);
    } catch (e) {
        console.log(e.stack);
    }
}

Browser.prototype.realMakeTier = function(source) {
    var thisB = this;
    var background = this.tierBackgroundColors[this.tiers.length % this.tierBackgroundColors.length];

    var viewport = makeElement('canvas', null, 
                               {width: '' + ((this.featurePanelWidth|0) + 2000), height: "50"}, 
                               {position: 'absolute', 
                                padding: '0px', 
                                margin: '0px',
                                border: '0px', 
                                left: '-1000px', /* borderTopStyle: 'solid', borderTopColor: 'black', */ 
                                borderBottomStyle: 'solid', 
                                borderBottomColor: 'rgb(180,180,180)', 
                                borderRightStyle: 'solid', 
                                borderRightColor: 'rgb(180,180,180)'});

    var viewportOverlay = makeElement('canvas', null,
         {width: + ((this.featurePanelWidth|0) + 2000), height: "50"}, 
         {position: 'relative', 
          padding: '0px', 
          margin: '0px',
          border: '0px', 
          left: '-1000px',
          zIndex: '1000',
          pointerEvents: 'none'});

    var placardContent = makeElement('span', 'blah');
    var placard = makeElement('div', [makeElement('i', null, {className: 'icon-warning-sign'}), ' ', placardContent], {}, {
        display: 'none',
        position: 'relative',
//        width: '100%',
        borderCollapse: 'collapse',
        marginTop: '-1px',
        height: '50px',
        textAlign: 'center',
        lineHeight: '50px',
        borderStyle: 'solid',
        borderColor: 'red',
        borderWidth: '1px'});
    
    var vph = makeElement('div', [viewport, viewportOverlay], {}, {display: 'inline-block', position: 'relative', width: '100%' , overflowX: 'hidden', overflowY: 'hidden'});
    // vph.className = 'tier-viewport-background';
    vph.style.background = background;

    vph.addEventListener('touchstart', function(ev) {return thisB.touchStartHandler(ev)}, false);
    vph.addEventListener('touchmove', function(ev) {return thisB.touchMoveHandler(ev)}, false);
    vph.addEventListener('touchend', function(ev) {return thisB.touchEndHandler(ev)}, false);
    vph.addEventListener('touchcancel', function(ev) {return thisB.touchCancelHandler(ev)}, false); 

    var tier = new DasTier(this, source, viewport, vph, viewportOverlay, placard, placardContent);
    tier.oorigin = this.viewStart;
    tier.background = background;

    tier.quantOverlay = makeElement(
        'canvas', null, 
        {width: '50', height: "56"}, 
        {position: 'absolute', 
         padding: '0px', 
         margin: '0px',
         border: '0px', 
         left: '' + ((this.featurePanelWidth/2)|0) + 'px',
         top: '0px',
         display: 'none'});
    tier.holder.appendChild(tier.quantOverlay);
    
    var isDragging = false;
    var dragOrigin, dragMoveOrigin;
    var hoverTimeout;

    var featureLookup = function(rx, ry) {
        var st = tier.subtiers;
        if (!st) {
            return;
        }

        var sti = 0;
        ry -= MIN_PADDING;
        while (sti < st.length && ry > st[sti].height && sti < (st.length - 1)) {
            ry = ry - st[sti].height - MIN_PADDING;
            ++sti;
        }
        if (sti >= st.length) {
            return;
        }

        var glyphs = st[sti].glyphs;
        var viewCenter = (thisB.viewStart + thisB.viewEnd)/2;
        var offset = (tier.glyphCacheOrigin - thisB.viewStart)*thisB.scale;
        rx -= offset;
       
        return glyphLookup(glyphs, rx);
    }

    var dragMoveHandler = function(ev) {
        ev.preventDefault(); ev.stopPropagation();
        var rx = ev.clientX;
        if (tier.dasSource.tier_type !== 'sequence' && rx != dragMoveOrigin) {
            thisB.move((rx - dragMoveOrigin));
            dragMoveOrigin = rx;
        }
        thisB.isDragging = true;
    }

    var dragUpHandler = function(ev) {
        window.removeEventListener('mousemove', dragMoveHandler, true);
        window.removeEventListener('mouseup', dragUpHandler, true);
        // thisB.isDragging = false;    // Can't clear here before the per-tier mouseups get called later :-(.
                                        // Shouldn't matter because cleared on next mousedown. 
    }
        

    vph.addEventListener('mousedown', function(ev) {
        thisB.browserHolder.focus();
        ev.preventDefault();
        var br = vph.getBoundingClientRect();
        var rx = ev.clientX, ry = ev.clientY;

        window.addEventListener('mousemove', dragMoveHandler, true);
        window.addEventListener('mouseup', dragUpHandler, true);
        dragOrigin = dragMoveOrigin = rx;
        thisB.isDragging = false; // Not dragging until a movement event arrives.
    }, false);

    vph.addEventListener('mousemove', function(ev) {
        var br = vph.getBoundingClientRect();
        var rx = ev.clientX - br.left, ry = ev.clientY - br.top;

        if (hoverTimeout) {
            clearTimeout(hoverTimeout);
        }

        if (isDragging) {
            // if (tier.dasSource.tier_type !== 'sequence' && rx != dragMoveOrigin) {
            //    thisB.move((rx - dragMoveOrigin));
            //    dragMoveOrigin = rx;
            // }
        } else {
            hoverTimeout = setTimeout(function() {
                var hit = featureLookup(rx, ry);
                if (hit && hit.length > 0) {
                    thisB.notifyFeatureHover(ev, hit[hit.length - 1], hit, tier);
                }
            }, 1000);
        }
    });

    var doubleClickTimeout = null;
    vph.addEventListener('mouseup', function(ev) {
        var br = vph.getBoundingClientRect();
        var rx = ev.clientX - br.left, ry = ev.clientY - br.top;

        var hit = featureLookup(rx, ry);
        if (hit && hit.length > 0 && !thisB.isDragging) {
            if (doubleClickTimeout) {
                clearTimeout(doubleClickTimeout);
                doubleClickTimeout = null;
                thisB.featureDoubleClick(hit, rx, ry);
            } else {
                doubleClickTimeout = setTimeout(function() {
                    doubleClickTimeout = null;
                    thisB.notifyFeature(ev, hit[hit.length-1], hit, tier);
                }, 500);
            }
        }

        if (thisB.isDragging && rx != dragOrigin && tier.dasSource.tier_type === 'sequence') {
            var a = thisB.viewStart + (rx/thisB.scale);
            var b = thisB.viewStart + (dragOrigin/thisB.scale);

            var min, max;
            if (a < b) {
                min = a|0; max = b|0;
            } else {
                min = b|0; max = a|0;
            }

            thisB.notifyRegionSelect(thisB.chr, min, max);
        }
        thisB.isDragging = false;
    }, false);

    vph.addEventListener('mouseout', function(ev) {
        isDragging = false;
    });



    tier.removeButton = makeElement('i', null, {className: 'icon-remove'});
    tier.bumpButton = makeElement('i', null, {className: 'icon-plus-sign'});
    tier.loaderButton = makeElement('img', null, {src: this.uiPrefix + 'img/loader.gif'}, {display: 'none'});
    tier.infoElement = makeElement('div', tier.dasSource.desc, {}, {display: 'none', maxWidth: '200px', whiteSpace: 'normal', color: 'rgb(100,100,100)'});
    tier.nameButton = makeElement('a', [tier.removeButton, makeElement('span', [source.name, tier.infoElement], {}, {display: 'inline-block', marginLeft: '5px', marginRight: '5px'}), tier.bumpButton, tier.loaderButton], {className: 'tier-tab'});
    
    tier.label = makeElement('span',
       [tier.nameButton],
       {className: 'btn-group'},
       {zIndex: 1001, position: 'absolute', left: '2px', top: '2px', opacity: 0.8, display: 'inline-block'});
    var row = makeElement('div', [vph, placard , tier.label ], {}, {position: 'relative', display: 'block' /*, transition: 'height 0.5s' */});
    tier.row = row;


    tier.removeButton.addEventListener('click', function(ev) {
        ev.stopPropagation(); ev.preventDefault();
        thisB.removeTier(source);
    }, false);
    tier.nameButton.addEventListener('click', function(ev) {
        ev.stopPropagation(); ev.preventDefault();
        for (var ti = 0; ti < thisB.tiers.length; ++ti) {
            if (thisB.tiers[ti] === tier) {
                thisB.browserHolder.focus();
                if (ti != thisB.selectedTier) {
                    thisB.setSelectedTier(ti);
                    return;
                }
            }
        }

        if (!tier.infoVisible) {
            tier.infoElement.style.display = 'block';
            tier.updateHeight();
            tier.infoVisible = true;
        } else {
            tier.infoElement.style.display = 'none';
            tier.updateHeight();
            tier.infoVisible = false;
        }
    }, false);
    tier.bumpButton.addEventListener('click', function(ev) {
        ev.stopPropagation(); ev.preventDefault();
        var bumpStatus;
        var t = tier;
        if (t.dasSource.collapseSuperGroups) {
            
            if (bumpStatus === undefined) {
                bumpStatus = !t.bumped;
            }
            t.bumped = bumpStatus;
            t.layoutWasDone = false;
            t.draw();
            
            t.updateLabel();
        }
    }, false);

    
    var dragLabel;
    var tierOrdinal;
    var yAtLastReorder;
    var tiersWereReordered = false;

    var labelDragHandler = function(ev) {
        var label = tier.label;
        ev.stopPropagation(); ev.preventDefault();
        if (!dragLabel) {
            dragLabel = label.cloneNode(true);
            dragLabel.style.cursor = 'pointer';
            thisB.svgHolder.appendChild(dragLabel);
            label.style.visibility = 'hidden';
            

            for (var ti = 0; ti < thisB.tiers.length; ++ti) {
                if (thisB.tiers[ti] === tier) {
                    tierOrdinal = ti;
                    break;
                }
            }

            yAtLastReorder = ev.clientY;
        }
        dragLabel.style.left = label.getBoundingClientRect().left + 'px'; dragLabel.style.top = ev.clientY - 10 + 'px';
        
        var pty = ev.clientY - thisB.tierHolder.getBoundingClientRect().top;
        for (var ti = 0; ti < thisB.tiers.length; ++ti) {
            var tt = thisB.tiers[ti];
            var ttr = tt.row.getBoundingClientRect();
            pty -= (ttr.bottom - ttr.top);
            if (pty < 0) {
                if (ti < tierOrdinal && ev.clientY < yAtLastReorder || ti > tierOrdinal && ev.clientY > yAtLastReorder) {
                    var st = thisB.tiers[thisB.selectedTier];

                    thisB.tiers.splice(tierOrdinal, 1);
                    thisB.tiers.splice(ti, 0, tier);
                    var ts = thisB.sources[tierOrdinal];
                    thisB.sources.splice(tierOrdinal, 1);
                    thisB.sources.splice(ti, 0, ts);

                    // FIXME probably shouldn't be recorded selected tier by index (!)
                    for (var sti = 0; sti < thisB.tiers.length; ++sti) {
                        if (thisB.tiers[sti] === st) {
                            thisB.selectedTier = sti; break;
                        }
                    }

                    tierOrdinal = ti;
                    yAtLastReorder = ev.clientY;
                    removeChildren(thisB.tierHolder);
                    for (var i = 0; i < thisB.tiers.length; ++i) {
                        thisB.tierHolder.appendChild(thisB.tiers[i].row);
                    }
                    thisB.tierHolder.appendChild(thisB.ruler);
                    tiersWereReordered = true;
                    thisB.arrangeTiers();
                }
                break;
            }
        }
    };

    var labelReleaseHandler = function(ev) {
        var label = tier.label;
        ev.stopPropagation(); ev.preventDefault();
        if (dragLabel) {
            dragLabel.style.cursor = 'auto';
            thisB.svgHolder.removeChild(dragLabel);
            dragLabel = null;
            label.style.visibility = null;
        }
        document.removeEventListener('mousemove', labelDragHandler, false);
        document.removeEventListener('mouseup', labelReleaseHandler, false);

        if (tiersWereReordered)
            thisB.notifyTier();
    };

    tier.label.addEventListener('mousedown', function(ev) {
        ev.stopPropagation(); ev.preventDefault();
        tiersWereReordered = false;
        document.addEventListener('mousemove', labelDragHandler, false);
        document.addEventListener('mouseup', labelReleaseHandler, false);
    }, false);

    this.tierHolder.appendChild(row);    
    this.tiers.push(tier);  // NB this currently tells any extant knownSpace about the new tier.
    
    tier.init(); // fetches stylesheet
    this.arrangeTiers();
    tier.updateLabel();
}

Browser.prototype.refreshTier = function(tier) {
    if (this.knownSpace) {
        this.knownSpace.invalidate(tier);
    }
}

Browser.prototype.arrangeTiers = function() {
    for (var ti = 0; ti < this.tiers.length; ++ti) {
        var t = this.tiers[ti];
        t.background = this.tierBackgroundColors[ti % this.tierBackgroundColors.length];
        t.holder.style.background = t.background;
    }
}



Browser.prototype.refresh = function() {
    this.notifyLocation();
    var width = (this.viewEnd - this.viewStart) + 1;
    /* var minExtraW = (width * this.minExtra) | 0;
    var maxExtraW = (width * this.maxExtra) | 0;*/
    var minExtraW = (100.0/this.scale)|0;
    var maxExtraW = (1000.0/this.scale)|0;

    var newOrigin = (this.viewStart + this.viewEnd) / 2;
    var oh = newOrigin - this.origin;
    this.origin = newOrigin;
    this.scaleAtLastRedraw = this.scale;
    for (var t = 0; t < this.tiers.length; ++t) {
        var od = oh;
        if (this.tiers[t].originHaxx) {
            od += this.tiers[t].originHaxx;
        }
        this.tiers[t].originHaxx = od;
    }

    var scaledQuantRes = this.targetQuantRes / this.scale;

    var innerDrawnStart = Math.max(1, (this.viewStart|0) - minExtraW);
    var innerDrawnEnd = Math.min((this.viewEnd|0) + minExtraW, ((this.currentSeqMax|0) > 0 ? (this.currentSeqMax|0) : 1000000000))
    var outerDrawnStart = Math.max(1, (this.viewStart|0) - maxExtraW);
    var outerDrawnEnd = Math.min((this.viewEnd|0) + maxExtraW, ((this.currentSeqMax|0) > 0 ? (this.currentSeqMax|0) : 1000000000));

    if (!this.knownSpace || this.knownSpace.chr !== this.chr) {
        var ss = null;
        for (var i = 0; i < this.tiers.length; ++i) {
            if (this.tiers[i].sequenceSource) {
                ss = this.tiers[i].sequenceSource;
                break;
            }
        }
        this.knownSpace = new KnownSpace(this.tiers, this.chr, outerDrawnStart, outerDrawnEnd, scaledQuantRes, ss);
    }
    
    var seg = this.knownSpace.bestCacheOverlapping(this.chr, innerDrawnStart, innerDrawnEnd);
    if (seg && seg.min <= innerDrawnStart && seg.max >= innerDrawnEnd) {
        this.drawnStart = Math.max(seg.min, outerDrawnStart);
        this.drawnEnd = Math.min(seg.max, outerDrawnEnd);
    } else {
        this.drawnStart = outerDrawnStart;
        this.drawnEnd = outerDrawnEnd;
    }
    
    this.knownSpace.viewFeatures(this.chr, this.drawnStart, this.drawnEnd, scaledQuantRes);
    this.drawOverlays();
}

function setSources(msh, availableSources, maybeMapping) {
    if (maybeMapping) {
        for (var s = 0; s < availableSources.length; ++s) {
            availableSources[s].mapping = maybeMapping;
        }
    }
    msh.set(availableSources);
}

Browser.prototype.queryRegistry = function(maybeMapping, tryCache) {
    var thisB = this;
    var coords, msh;
    if (maybeMapping) {
        coords = this.chains[maybeMapping].coords;
        if (!thisB.mappableSources[maybeMapping]) {
            thisB.mappableSources[maybeMapping] = new Observed();
        }
        msh = thisB.mappableSources[maybeMapping];
    } else {
        coords = this.coordSystem;
        msh = this.availableSources;
    }
    var cacheHash = hex_sha1(miniJSONify(coords));
    if (tryCache) {
        var cacheTime = localStorage['dalliance.registry.' + cacheHash + '.last_queried'];
        if (cacheTime) {
            try {
                setSources(msh, JSON.parse(localStorage['dalliance.registry.' + cacheHash + '.sources']), maybeMapping);
                var cacheAge = (Date.now()|0) - (cacheTime|0);
                if (cacheAge < (12 * 60 * 60 * 1000)) {
                    // alert('Using cached registry data');
                    return;
                } else {
                    // alert('Registry data is stale, refetching');
                }
            } catch (rex) {
                console.log('Bad registry cache: ' + rex);
            }
        }
    }
            
    new DASRegistry(this.registry).sources(function(sources) {
        var availableSources = [];
        for (var s = 0; s < sources.length; ++s) {
            var source = sources[s];
            if (!source.coords || source.coords.length == 0) {
                continue;
            }
            var scoords = source.coords[0];
            if (scoords.taxon != coords.taxon || scoords.auth != coords.auth || scoords.version != coords.version) {
                continue;
            }   
            availableSources.push(source);
        }

        localStorage['dalliance.registry.' + cacheHash + '.sources'] = JSON.stringify(availableSources);
        localStorage['dalliance.registry.' + cacheHash + '.last_queried'] = '' + Date.now();
        
        setSources(msh, availableSources, maybeMapping);
    }, function(error) {
        // msh.set(null);
    }, coords);
}

//
// Navigation
//

Browser.prototype.move = function(pos)
{
    var wid = this.viewEnd - this.viewStart;
    this.viewStart -= pos / this.scale;
    this.viewEnd = this.viewStart + wid;
    if (this.currentSeqMax > 0 && this.viewEnd > this.currentSeqMax) {
        this.viewEnd = this.currentSeqMax;
        this.viewStart = this.viewEnd - wid;
    }
    if (this.viewStart < 1) {
        this.viewStart = 1;
        this.viewEnd = this.viewStart + wid;
    }
    this.notifyLocation();
    
    var viewCenter = (this.viewStart + this.viewEnd)/2;
    
    for (var i = 0; i < this.tiers.length; ++i) {
        var offset = (this.viewStart - this.tiers[i].norigin)*this.scale;
	this.tiers[i].viewport.style.left = '' + ((-offset|0) - 1000) + 'px';
        var ooffset = (this.viewStart - this.tiers[i].oorigin)*this.scale;
        this.tiers[i].overlay.style.left = '' + ((-ooffset|0) - 1000) + 'px';
    }

    this.spaceCheck();
}

Browser.prototype.zoomStep = function(delta) {
    var oz = 1.0 * this.zoomSliderValue;
    var nz = oz + delta;
    if (nz < this.zoomMin) {
        nz= this.zoomMin;
    }
    if (nz > this.zoomMax) {
        nz = this.zoomMax;
    }

    if (nz != oz) {
        this.zoomSliderValue = nz; // FIXME maybe ought to set inside zoom!
        this.zoom(Math.exp((1.0 * nz) / this.zoomExpt));
    }
}

Browser.prototype.zoom = function(factor) {
    this.zoomFactor = factor;
    var viewCenter = Math.round((this.viewStart + this.viewEnd) / 2.0)|0;
    this.viewStart = viewCenter - this.zoomBase * this.zoomFactor / 2;
    this.viewEnd = viewCenter + this.zoomBase * this.zoomFactor / 2;
    if (this.currentSeqMax > 0 && (this.viewEnd > this.currentSeqMax + 5)) {
        var len = this.viewEnd - this.viewStart + 1;
        this.viewEnd = this.currentSeqMax;
        this.viewStart = this.viewEnd - len + 1;
    }
    if (this.viewStart < 1) {
        var len = this.viewEnd - this.viewStart + 1;
        this.viewStart = 1;
        this.viewEnd = this.viewStart + len - 1;
    }
    this.scale = this.featurePanelWidth / (this.viewEnd - this.viewStart)
    var width = this.viewEnd - this.viewStart + 1;
    
    var scaleRat = (this.scale / this.scaleAtLastRedraw);

    this.refresh();
}

Browser.prototype.spaceCheck = function(dontRefresh) {
    if (!this.knownSpace || this.knownSpace.chr !== this.chr) {
        this.refresh();
        return;
    } 

    var width = ((this.viewEnd - this.viewStart)|0) + 1;
    // var minExtraW = (width * this.minExtra) | 0;
    // var maxExtraW = (width * this.maxExtra) | 0;
    var minExtraW = (100.0/this.scale)|0;
    var maxExtraW = (1000.0/this.scale)|0;

    if ((this.drawnStart|0) > Math.max(1, ((this.viewStart|0) - minExtraW)|0)  || (this.drawnEnd|0) < Math.min((this.viewEnd|0) + minExtraW, ((this.currentSeqMax|0) > 0 ? (this.currentSeqMax|0) : 1000000000)))  {
        this.refresh();
    }
}




Browser.prototype.resizeViewer = function(skipRefresh) {
    var width = this.tierHolder.getBoundingClientRect().width | 0;

    var oldFPW = this.featurePanelWidth;
    this.featurePanelWidth = width|0;

    if (oldFPW != this.featurePanelWidth) {
        var viewWidth = this.viewEnd - this.viewStart;
        var nve = this.viewStart + (viewWidth * this.featurePanelWidth) / oldFPW;


        // var delta = nve - this.viewEnd;
        // this.viewStart = this.viewStart - (delta/2);
        // this.viewEnd = this.viewEnd + (delta/2);

        this.viewEnd = nve;

        var wid = this.viewEnd - this.viewStart + 1;
        if (this.currentSeqMax > 0 && this.viewEnd > this.currentSeqMax) {
            this.viewEnd = this.currentSeqMax;
            this.viewStart = this.viewEnd - wid + 1;
        }
        if (this.viewStart < 1) {
            this.viewStart = 1;
            this.viewEnd = this.viewStart + wid - 1;
        }

        this.positionRuler();

        if (!skipRefresh) {
            this.spaceCheck();
        }
        this.notifyLocation();
    }
}

Browser.prototype.addTier = function(conf) {
    this.sources.push(conf);
    this.makeTier(conf);
    this.positionRuler();
    this.notifyTier();
}

Browser.prototype.removeTier = function(conf) {
    var target = -1;

    // FIXME can this be done in a way that doesn't need changing every time we add
    // new datasource types.

    if (typeof conf.index !== 'undefined' && conf.index >=0 && conf.index < this.tiers.length) {
        target = conf.index;
    } else {
        for (var ti = 0; ti < this.tiers.length; ++ti) {
            var ts = this.tiers[ti].dasSource;
            if ((conf.uri && ts.uri === conf.uri) ||
                (conf.bwgURI && ts.bwgURI === conf.bwgURI) ||
                (conf.bamURI && ts.bamURI === conf.bamURI) ||
                (conf.twoBitURI && ts.twoBitURI === conf.twoBitURI))
            {
                 if (ts.stylesheet_uri == conf.stylesheet_uri) {
                    target = ti; break;
                }
            }
        }
    }

    if (target < 0) {
        throw "Couldn't find requested tier";
    }

    var victim = this.tiers[target];
    this.tierHolder.removeChild(victim.row);
    this.tiers.splice(target, 1);
    this.sources.splice(target, 1);

    this.arrangeTiers();
    
    this.notifyTier();
}


Browser.prototype.setLocation = function(newChr, newMin, newMax, callback) {
    if (!callback) {
        callback = function(err) {
            if (err) {
                throw err;
            }
        }
    }
    var thisB = this;

    if (!newChr || newChr == this.chr) {
        return this._setLocation(null, newMin, newMax, null, callback);
    } else {
        var ss;
        for (var ti = 0; ti < this.tiers.length; ++ti) {
            if (this.tiers[ti].sequenceSource) {
                ss = this.tiers[ti].sequenceSource;
                break;
            }
        }
        if (!ss) {
            return callback('Need a sequence source');
        }

        ss.getSeqInfo(newChr, function(si) {
            if (!si) {
                var altChr;
                if (newChr.indexOf('chr') == 0) {
                    altChr = newChr.substr(3);
                } else {
                    altChr = 'chr' + newChr;
                }
                ss.getSeqInfo(altChr, function(si2) {
                    if (!si2) {
                        return callback("Couldn't find sequence '" + newChr + "'");
                    } else {
                        return thisB._setLocation(altChr, newMin, newMax, si2, callback);
                    }
                });
            } else {
                return thisB._setLocation(newChr, newMin, newMax, si, callback);
            }
        });
    }
}


Browser.prototype._setLocation = function(newChr, newMin, newMax, newChrInfo, callback) {
    if (newChr) {
        if (newChr.indexOf('chr') == 0)
            newChr = newChr.substring(3);

        this.chr = newChr;
        this.currentSeqMax = newChrInfo.length;
    }

    newMin|=0; newMax|=0;
    var newWidth = Math.max(10, newMax-newMin+1);
    if (newMin < 1) {
        newMin = 1; newMax = newMin + newWidth - 1;
    }
    if (newMax > this.currentSeqMax) {
        newMax = this.currentSeqMax;
        newMin = Math.max(1, newMax - newWidth + 1);
    }

    this.viewStart = newMin;
    this.viewEnd = newMax;
    var newScale = this.featurePanelWidth / (this.viewEnd - this.viewStart);
    var scaleChanged = (Math.abs(newScale - this.scale)) > 0.0001;
    this.scale = newScale;
    this.zoomSliderValue = this.zoomExpt * Math.log((this.viewEnd - this.viewStart + 1) / this.zoomBase);
    this.isSnapZooming = false;
    this.savedZoom = null;
    this.notifyLocation();

    if (scaleChanged) {
        this.refresh();
    } else {
        var viewCenter = (this.viewStart + this.viewEnd)/2;
    
        for (var i = 0; i < this.tiers.length; ++i) {
            var offset = (this.viewStart - this.tiers[i].norigin)*this.scale;
	    this.tiers[i].viewport.style.left = '' + ((-offset|0) - 1000) + 'px';
            var ooffset = (this.viewStart - this.tiers[i].oorigin)*this.scale;
            this.tiers[i].overlay.style.left = '' + ((-ooffset|0) - 1000) + 'px';
        }
    }

    this.spaceCheck();
    return callback();
}

Browser.prototype.addFeatureListener = function(handler, opts) {
    opts = opts || {};
    this.featureListeners.push(handler);
}

Browser.prototype.notifyFeature = function(ev, feature, hit, tier) {
  for (var fli = 0; fli < this.featureListeners.length; ++fli) {
      try {
          this.featureListeners[fli](ev, feature, hit, tier);
      } catch (ex) {
          console.log(ex.stack);
      }
  }
}

Browser.prototype.addFeatureHoverListener = function(handler, opts) {
    opts = opts || {};
    this.featureHoverListeners.push(handler);
}

Browser.prototype.notifyFeatureHover = function(ev, feature, hit, tier) {
    for (var fli = 0; fli < this.featureHoverListeners.length; ++fli) {
        try {
            this.featureHoverListeners[fli](ev, feature, hit, tier);
        } catch (ex) {
            console.log(ex.stack);
        }
    }
}

Browser.prototype.addViewListener = function(handler, opts) {
    opts = opts || {};
    this.viewListeners.push(handler);
}

Browser.prototype.notifyLocation = function() {
    for (var lli = 0; lli < this.viewListeners.length; ++lli) {
        try {
            this.viewListeners[lli](this.chr, this.viewStart|0, this.viewEnd|0, this.zoomSliderValue, {current: this.zoomSliderValue, min: this.zoomMin, max: this.zoomMax});
        } catch (ex) {
            console.log(ex.stack);
        }
    }
}

Browser.prototype.addTierListener = function(handler) {
    this.tierListeners.push(handler);
}

Browser.prototype.notifyTier = function() {
    for (var tli = 0; tli < this.tierListeners.length; ++tli) {
        try {
            this.tierListeners[tli]();
        } catch (ex) {
            console.log(ex.stack);
        }
    }
}

Browser.prototype.addRegionSelectListener = function(handler) {
    this.regionSelectListeners.push(handler);
}

Browser.prototype.notifyRegionSelect = function(chr, min, max) {
    for (var rli = 0; rli < this.regionSelectListeners.length; ++rli) {
        try {
            this.regionSelectListeners[rli](chr, min, max);
        } catch (ex) {
            console.log(ex.stack);
        }
    }
}


Browser.prototype.highlightRegion = function(chr, min, max) {
    this.highlights.push(new Region(chr, min, max));
    var visStart = this.viewStart - (1000/this.scale);
    var visEnd = this.viewEnd + (1000/this.scale);
    if (chr == this.chr && min < visEnd && max > visStart) {
        this.drawOverlays();
    }
}

Browser.prototype.drawOverlays = function() {
    for (var ti = 0; ti < this.tiers.length; ++ti) {
        this.tiers[ti].drawOverlay();
    }
}

Browser.prototype.featuresInRegion = function(chr, min, max) {
    var features = [];
    if (chr !== this.chr) {
        return [];
    }

    for (var ti = 0; ti < this.tiers.length; ++ti) {
        var fl = this.tiers[ti].currentFeatures || [];
        for (var fi = 0; fi < fl.length; ++fi) {
            var f = fl[fi];
            if (f.min <= max && f.max >= min) {
                features.push(f);
            }
        }
    }
    return features;
}

Browser.prototype.setSelectedTier = function(t) {
    this.selectedTier = t;
    for (var ti = 0; ti < this.tiers.length; ++ti) {
        var button = this.tiers[ti].nameButton;

        if (ti == this.selectedTier) {
            button.classList.add('active');
            // this.tiers[ti].label.focus();
        } else {
            button.classList.remove('active');
        }
    }
    if (t != null) {
        this.browserHolder.focus();
    }
}

Browser.prototype.addTierSelectionWrapListener = function(f) {
    this.tierSelectionWrapListeners.push(f);
}

Browser.prototype.notifyTierSelectionWrap = function(i) {
    for (var fli = 0; fli < this.tierSelectionWrapListeners.length; ++fli) {
        try {
            this.tierSelectionWrapListeners[fli](i);
        } catch (ex) {
            console.log(ex.stack);
        }
    }
}

Browser.prototype.positionRuler = function() {
    var display = 'none';
    var left = '';
    var right = '';

    if (this.rulerLocation == 'center') {
        display = 'block';
        left = '' + ((this.featurePanelWidth/2)|0) + 'px';
    } else if (this.rulerLocation == 'left') {
        display = 'block';
        left = '0px';
    } else if (this.rulerLocation == 'right') {
        display = 'block';
        right = '0px'
    } else {
        display = 'none';
    }

    this.ruler.style.display = display;
    this.ruler.style.left = left;
    this.ruler.style.right = right;

    for (var ti = 0; ti < this.tiers.length; ++ti) {
        var q = this.tiers[ti].quantOverlay;
        if (q) {
            q.style.display = display;
            q.style.left = left;
            q.style.right = right;
        }
    }
}

Browser.prototype.featureDoubleClick = function(hit, rx, ry) {
    if (!hit || hit.length == 0)
        return;

    f = hit[hit.length - 1];

    if (!f.min || !f.max) {
        return;
    }

    var fstart = (((f.min|0) - (this.viewStart|0)) * this.scale);
    var fwidth = (((f.max - f.min) + 1) * this.scale);
    
    var newMid = (((f.min|0) + (f.max|0)))/2;
    if (fwidth > 10) {
        var frac = (1.0 * (rx - fstart)) / fwidth;
        if (frac < 0.3) {
            newMid = (f.min|0);
        } else  if (frac > 0.7) {
            newMid = (f.max|0) + 1;
        }
    }

    var width = this.viewEnd - this.viewStart;
    this.setLocation(null, newMid - (width/2), newMid + (width/2));
}

function glyphLookup(glyphs, rx, matches) {
    matches = matches || [];

    for (var gi = 0; gi < glyphs.length; ++gi) {
        var g = glyphs[gi];
        if (!g.notSelectable && g.min() <= rx && g.max() >= rx) {
            if (g.feature) {
                matches.push(g.feature);
            } else if (g.group) {
                matches.push(g.group);
            }
    
            if (g.glyphs) {
                return glyphLookup(g.glyphs, rx, matches);
            } else if (g.glyph) {
                return glyphLookup([g.glyph], rx, matches);
            } else {
                return matches;
            }
        }
    }
    return matches;
}
/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2011
//
// feature-popup.js
//

var TAGVAL_NOTE_RE = new RegExp('^([A-Za-z]+)=(.+)');

Browser.prototype.addFeatureInfoPlugin = function(handler) {
    if (!this.featureInfoPlugins) {
        this.featureInfoPlugins = [];
    }
    this.featureInfoPlugins.push(handler);
}

function FeatureInfo(hit, feature, group) {
    var name = pick(group.type, feature.type);
    var fid = pick(group.label, feature.label, group.id, feature.id);
    if (fid && fid.indexOf('__dazzle') != 0) {
        name = name + ': ' + fid;
    }

    this.hit = hit;
    this.feature = feature;
    this.group = group;
    this.title = name;
    this.sections = [];
}

FeatureInfo.prototype.setTitle = function(t) {
    this.title = t;
}

FeatureInfo.prototype.add = function(label, info) {
    if (typeof info === 'string') {
        info = makeElement('span', info);
    }
    this.sections.push({label: label, info: info});
}

Browser.prototype.featurePopup = function(ev, __ignored_feature, hit, tier) {
    var hi = hit.length;
    var feature = --hi >= 0 ? hit[hi] : {};
    var group = --hi >= 0 ? hit[hi] : {};

    var featureInfo = new FeatureInfo(hit, feature, group);
    var fips = this.featureInfoPlugins || [];
    for (fipi = 0; fipi < fips.length; ++fipi) {
        try {
            fips[fipi](feature, featureInfo);
        } catch (e) {
            console.log(e.stack || e);
        }
    }
    fips = tier.featureInfoPlugins || [];
    for (fipi = 0; fipi < fips.length; ++fipi) {
        try {
            fips[fipi](feature, featureInfo);
        } catch (e) {
            console.log(e.stack || e);
        }
    }

    this.removeAllPopups();

    var table = makeElement('table', null, {className: 'table table-striped table-condensed'});
    table.style.width = '100%';
    table.style.margin = '0px';

    var idx = 0;
    if (feature.method) {
        var row = makeElement('tr', [
            makeElement('th', 'Method'),
            makeElement('td', feature.method)
        ]);
        table.appendChild(row);
        ++idx;
    }
    {
        var loc;
        if (group.segment) {
            loc = group;
        } else {
            loc = feature;
        }
        var row = makeElement('tr', [
            makeElement('th', 'Location'),
            makeElement('td', loc.segment + ':' + loc.min + '-' + loc.max)
        ]);
        row.style.backgroundColor = this.tierBackgroundColors[idx % this.tierBackgroundColors.length];
        table.appendChild(row);
        ++idx;
    }
    if (feature.score !== undefined && feature.score !== null && feature.score != '-') {
        var row = makeElement('tr', [
            makeElement('th', 'Score'),
            makeElement('td', '' + feature.score)
        ]);
        table.appendChild(row);
        ++idx;
    }
    {
        var links = maybeConcat(group.links, feature.links);
        if (links && links.length > 0) {
            var row = makeElement('tr', [
                makeElement('th', 'Links'),
                makeElement('td', links.map(function(l) {
                    return makeElement('div', makeElement('a', l.desc, {href: l.uri, target: '_new'}));
                }))
            ]);
            table.appendChild(row);
            ++idx;
        }
    }
    {
        var notes = maybeConcat(group.notes, feature.notes);
        for (var ni = 0; ni < notes.length; ++ni) {
            var k = 'Note';
            var v = notes[ni];
            var m = v.match(TAGVAL_NOTE_RE);
            if (m) {
                k = m[1];
                v = m[2];
            }

            var row = makeElement('tr', [
                makeElement('th', k),
                makeElement('td', v)
            ]);
            table.appendChild(row);
            ++idx;
        }
    }

    for (var fisi = 0; fisi < featureInfo.sections.length; ++fisi) {
        var section = featureInfo.sections[fisi];
        table.appendChild(makeElement('tr', [
            makeElement('th', section.label),
            makeElement('td', section.info)]));
    }        

    this.popit(ev, featureInfo.title, table, {width: 400});
}
/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2010
//
// chainset.js: liftover support
//

function Chainset(uri, srcTag, destTag, coords) {
    this.uri = uri;
    this.srcTag = srcTag;
    this.destTag = destTag;
    this.coords = coords;
    this.chainsBySrc = {};
    this.chainsByDest = {};
    this.postFetchQueues = {};
}

function parseCigar(cigar)
{
    var cigops = [];
    var CIGAR_REGEXP = new RegExp('([0-9]*)([MID])', 'g');
    var match;
    while ((match = CIGAR_REGEXP.exec(cigar)) != null) {
        var count = match[1];
        if (count.length == 0) {
            count = 1;
        }
        cigops.push({cnt: count|0, op: match[2]});
    }
    return cigops;
}

Chainset.prototype.fetchChainsTo = function(chr) {
    var thisCS = this;
    new DASSource(this.uri).alignments(chr, {}, function(aligns) {
        if (!thisCS.chainsByDest[chr]) {
            thisCS.chainsByDest[chr] = []; // prevent re-fetching.
        }

        for (var ai = 0; ai < aligns.length; ++ai) {
            var aln = aligns[ai];
            for (var bi = 0; bi < aln.blocks.length; ++bi) {
                var block = aln.blocks[bi];
                var srcSeg, destSeg;
                for (var si = 0; si < block.segments.length; ++si) {
                    var seg = block.segments[si];
                    var obj = aln.objects[seg.object];
                    if (obj.dbSource === thisCS.srcTag) {
                        srcSeg = seg;
                    } else if (obj.dbSource === thisCS.destTag) {
                        destSeg = seg;
                    }
                }
                if (srcSeg && destSeg) {
                    var chain = {
                        srcChr:     aln.objects[srcSeg.object].accession,
                        srcMin:     srcSeg.min|0,
                        srcMax:     srcSeg.max|0,
                        srcOri:     srcSeg.strand,
                        destChr:    aln.objects[destSeg.object].accession,
                        destMin:    destSeg.min|0,
                        destMax:    destSeg.max|0,
                        destOri:    destSeg.strand,
                        blocks:     []
                    }

                    var srcops = parseCigar(srcSeg.cigar), destops = parseCigar(destSeg.cigar);
                    var srcOffset = 0, destOffset = 0;
                    var srci = 0, desti = 0;
                    while (srci < srcops.length && desti < destops.length) {
                        if (srcops[srci].op == 'M' && destops[desti].op == 'M') {
                            var blockLen = Math.min(srcops[srci].cnt, destops[desti].cnt);
                            chain.blocks.push([srcOffset, destOffset, blockLen]);
                            if (srcops[srci].cnt == blockLen) {
                                ++srci;
                            } else {
                                srcops[srci].cnt -= blockLen;
                            }
                            if (destops[desti].cnt == blockLen) {
                                ++desti;
                            } else {
                                destops[desti] -= blockLen;
                            }
                            srcOffset += blockLen;
                            destOffset += blockLen;
                        } else if (srcops[srci].op == 'I') {
                            destOffset += srcops[srci++].cnt;
                        } else if (destops[desti].op == 'I') {
                            srcOffset += destops[desti++].cnt;
                        }
                    }

                    pusho(thisCS.chainsBySrc, chain.srcChr, chain);
                    pusho(thisCS.chainsByDest, chain.destChr, chain);
                }
            }
        }

        if (thisCS.postFetchQueues[chr]) {
            var pfq = thisCS.postFetchQueues[chr];
            for (var i = 0; i < pfq.length; ++i) {
                pfq[i]();
            }
            thisCS.postFetchQueues[chr] = null;
        }
    });
}

Chainset.prototype.mapPoint = function(chr, pos) {
    var chains = this.chainsBySrc[chr] || [];
    for (var ci = 0; ci < chains.length; ++ci) {
        var c = chains[ci];
        if (pos >= c.srcMin && pos <= c.srcMax) {
            var cpos;
            if (c.srcOri == '-') {
                cpos = c.srcMax - pos;
            } else {
                cpos = pos - c.srcMin;
            }
            var blocks = c.blocks;
            for (var bi = 0; bi < blocks.length; ++bi) {
                var b = blocks[bi];
                var bSrc = b[0];
                var bDest = b[1];
                var bSize = b[2];
                if (cpos >= bSrc && cpos <= (bSrc + bSize)) {
                    var apos = cpos - bSrc;

                    var dpos;
                    if (c.destOri == '-') {
                        dpos = c.destMax - bDest - apos;
                    } else {
                        dpos = apos + bDest + c.destMin;
                    }
                    return {seq: c.destChr, pos: dpos, flipped: (c.srcOri != c.destOri)}
                }
            }
        }
    }
    return null;
}

Chainset.prototype.unmapPoint = function(chr, pos) {
    var chains = this.chainsByDest[chr] || [];
    for (var ci = 0; ci < chains.length; ++ci) {
        var c = chains[ci];
        if (pos >= c.destMin && pos <= c.destMax) {
            var cpos;
            if (c.srcOri == '-') {
                cpos = c.destMax - pos;
            } else {
                cpos = pos - c.destMin;
            }    
            
            var blocks = c.blocks;
            for (var bi = 0; bi < blocks.length; ++bi) {
                var b = blocks[bi];
                var bSrc = b[0];
                var bDest = b[1];
                var bSize = b[2];
                if (cpos >= bDest && cpos <= (bDest + bSize)) {
                    var apos = cpos - bDest;

                    var dpos = apos + bSrc + c.srcMin;
                    var dpos;
                    if (c.destOri == '-') {
                        dpos = c.srcMax - bSrc - apos;
                    } else {
                        dpos = apos + bSrc + c.srcMin;
                    }
                    return {seq: c.srcChr, pos: dpos, flipped: (c.srcOri != c.destOri)}
                }
            }
            return null;
        }
    }
    return null;
}

Chainset.prototype.sourceBlocksForRange = function(chr, min, max, callback) {
    if (!this.chainsByDest[chr]) {
        var fetchNeeded = !this.postFetchQueues[chr];
        var thisCS = this;
        pusho(this.postFetchQueues, chr, function() {
            thisCS.sourceBlocksForRange(chr, min, max, callback);
        });
        if (fetchNeeded) {
            this.fetchChainsTo(chr);
        }
    } else {
        var mmin = this.unmapPoint(chr, min);
        var mmax = this.unmapPoint(chr, max);
        if (!mmin || !mmax || mmin.seq != mmax.seq) {
            callback([]);
        } else {
            callback([new DASSegment(mmin.seq, mmin.pos, mmax.pos)]);
        }
    }
}
function DColour(red, green, blue, name) {
    this.red = red|0;
    this.green = green|0;
    this.blue = blue|0;
    if (name) {
        this.name = name;
    }
}

DColour.prototype.toSvgString = function() {
    if (!this.name) {
        this.name = "rgb(" + this.red + "," + this.green + "," + this.blue + ")";
    }

    return this.name;
}

var palette = {
    red: new DColour(255, 0, 0, 'red'),
    green: new DColour(0, 255, 0, 'green'),
    blue: new DColour(0, 0, 255, 'blue'),
    yellow: new DColour(255, 255, 0, 'yellow'),
    white: new DColour(255, 255, 255, 'white'),
    black: new DColour(0, 0, 0, 'black')
};

var COLOR_RE = new RegExp('^#([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})([0-9A-Fa-f]{2})$');
var CSS_COLOR_RE = /rgb\(([0-9]+),([0-9]+),([0-9]+)\)/

function dasColourForName(name) {
    var c = palette[name];
    if (!c) {
        var match = COLOR_RE.exec(name);
        if (match) {
            c = new DColour(('0x' + match[1])|0, ('0x' + match[2])|0, ('0x' + match[3])|0, name);
            palette[name] = c;
        } else {
	    match = CSS_COLOR_RE.exec(name);
	    if (match) {
		c = new DColour(match[1]|0, match[2]|0, match[3]|0, name);
		palette[name] = c;
	    } else {
		console.log("couldn't handle color: " + name);
		c = palette.black;
		palette[name] = c;
	    }
        }
    }
    return c;
}

function makeGradient(steps, color1, color2, color3) {
    var cols = [];
    var c1 = dasColourForName(color1);
    var c2 = dasColourForName(color2);

    if (color3) {
	var c3 = dasColourForName(color3);
	for (var s = 0; s < steps; ++s) {
	    var relScore = (1.0 * s)/(steps-1);
	    var ca, cb, frac;
	    if (relScore < 0.5) {
                ca = c1;
                cb = c2;
                frac = relScore * 2;
            } else {
		ca = c2;
		cb = c3;
                frac = (relScore * 2.0) - 1.0;
            }
	    var fill = new DColour(
		((ca.red * (1.0 - frac)) + (cb.red * frac))|0,
		((ca.green * (1.0 - frac)) + (cb.green * frac))|0,
		((ca.blue * (1.0 - frac)) + (cb.blue * frac))|0
            ).toSvgString();
	    cols.push(fill);
	}
    } else {
	for (var s = 0; s < steps; ++s) {
	    var frac = (1.0 * s)/(steps-1);
	    var fill = new DColour(
		((c1.red * (1.0 - frac)) + (c2.red * frac))|0,
		((c1.green * (1.0 - frac)) + (c2.green * frac))|0,
		((c1.blue * (1.0 - frac)) + (c2.blue * frac))|0
            ).toSvgString();
	    cols.push(fill);
	}
    }
    return cols;
}
/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2010
//
// das.js: queries and low-level data model.
//

var dasLibErrorHandler = function(errMsg) {
    alert(errMsg);
}
var dasLibRequestQueue = new Array();



function DASSegment(name, start, end, description) {
    this.name = name;
    this.start = start;
    this.end = end;
    this.description = description;
}
DASSegment.prototype.toString = function() {
    return this.name + ':' + this.start + '..' + this.end;
};
DASSegment.prototype.isBounded = function() {
    return this.start && this.end;
}
DASSegment.prototype.toDASQuery = function() {
    var q = 'segment=' + this.name;
    if (this.start && this.end) {
        q += (':' + this.start + ',' + this.end);
    }
    return q;
}


function DASSource(a1, a2) {
    var options;
    if (typeof a1 == 'string') {
        this.uri = a1;
        options = a2 || {};
    } else {
        options = a1 || {};
    }
    for (var k in options) {
        if (typeof(options[k]) != 'function') {
            this[k] = options[k];
        }
    }


    if (!this.coords) {
        this.coords = [];
    }
    if (!this.props) {
        this.props = {};
    }

    // if (!this.uri || this.uri.length == 0) {
    //    throw "URIRequired";
    // }   FIXME
    if (this.uri && this.uri.substr(this.uri.length - 1) != '/') {
        this.uri = this.uri + '/';
    }
}

function DASCoords() {
}

function coordsMatch(c1, c2) {
    return c1.taxon == c2.taxon && c1.auth == c2.auth && c1.version == c2.version;
}

//
// DAS 1.6 entry_points command
//

DASSource.prototype.entryPoints = function(callback) {
    var dasURI = this.uri + 'entry_points';
    this.doCrossDomainRequest(dasURI, function(responseXML) {
            if (!responseXML) {
                return callback([]);
            }

                var entryPoints = new Array();
                
                var segs = responseXML.getElementsByTagName('SEGMENT');
                for (var i = 0; i < segs.length; ++i) {
                    var seg = segs[i];
                    var segId = seg.getAttribute('id');
                    
                    var segSize = seg.getAttribute('size');
                    var segMin, segMax;
                    if (segSize) {
                        segMin = 1; segMax = segSize|0;
                    } else {
                        segMin = seg.getAttribute('start');
                        if (segMin) {
                            segMin |= 0;
                        }
                        segMax = seg.getAttribute('stop');
                        if (segMax) {
                            segMax |= 0;
                        }
                    }
                    var segDesc = null;
                    if (seg.firstChild) {
                        segDesc = seg.firstChild.nodeValue;
                    }
                    entryPoints.push(new DASSegment(segId, segMin, segMax, segDesc));
                }          
               callback(entryPoints);
    });         
}

//
// DAS 1.6 sequence command
// Do we need an option to fall back to the dna command?
//

function DASSequence(name, start, end, alpha, seq) {
    this.name = name;
    this.start = start;
    this.end = end;
    this.alphabet = alpha;
    this.seq = seq;
}

DASSource.prototype.sequence = function(segment, callback) {
    var dasURI = this.uri + 'sequence?' + segment.toDASQuery();
    this.doCrossDomainRequest(dasURI, function(responseXML) {
        if (!responseXML) {
            callback([]);
            return;
        } else {
                var seqs = new Array();
                
                var segs = responseXML.getElementsByTagName('SEQUENCE');
                for (var i = 0; i < segs.length; ++i) {
                    var seg = segs[i];
                    var segId = seg.getAttribute('id');
                    var segMin = seg.getAttribute('start');
                    var segMax = seg.getAttribute('stop');
                    var segAlpha = 'DNA';
                    var segSeq = null;
                    if (seg.firstChild) {
                        var rawSeq = seg.firstChild.nodeValue;
                        segSeq = '';
                        var idx = 0;
                        while (true) {
                            var space = rawSeq.indexOf('\n', idx);
                            if (space >= 0) {
                                segSeq += rawSeq.substring(idx, space);
                                idx = space + 1;
                            } else {
                                segSeq += rawSeq.substring(idx);
                                break;
                            }
                        }
                    }
                    seqs.push(new DASSequence(segId, segMin, segMax, segAlpha, segSeq));
                }
                
                callback(seqs);
        }
    });
}

//
// DAS 1.6 features command
//

function DASFeature() {
    // We initialize these in the parser...
}

function DASGroup() {
    // We initialize these in the parser, too...
}

function DASLink(desc, uri) {
    this.desc = desc;
    this.uri = uri;
}

DASSource.prototype.features = function(segment, options, callback) {
    options = options || {};
    var thisB = this;

    var dasURI;
    if (this.features_uri) {
        dasURI = this.features_uri;
    } else {
        var filters = [];

        if (segment) {
            filters.push(segment.toDASQuery());
        } else if (options.group) {
            var g = options.group;
            if (typeof g == 'string') {
                filters.push('group_id=' + g);
            } else {
                for (var gi = 0; gi < g.length; ++gi) {
                    filters.push('group_id=' + g[gi]);
                }
            }
        }

        if (options.adjacent) {
            var adj = options.adjacent;
            if (typeof adj == 'string') {
                adj = [adj];
            }
            for (var ai = 0; ai < adj.length; ++ai) {
                filters.push('adjacent=' + adj[ai]);
            }
        }

        if (options.type) {
            if (typeof options.type == 'string') {
                filters.push('type=' + options.type);
            } else {
                for (var ti = 0; ti < options.type.length; ++ti) {
                    filters.push('type=' + options.type[ti]);
                }
            }
        }
        
        if (options.maxbins) {
            filters.push('maxbins=' + options.maxbins);
        }
        
        if (filters.length > 0) {
            dasURI = this.uri + 'features?' + filters.join(';');
        } else {
            callback([], 'No filters specified');
        }
    } 
   

    this.doCrossDomainRequest(dasURI, function(responseXML, req) {
        if (!responseXML) {
            var msg;
            if (req.status == 0) {
                msg = 'server may not support CORS';
            } else {
                msg = 'status=' + req.status;
            }
            callback([], 'Failed request: ' + msg);
            return;
        }
/*      if (req) {
            var caps = req.getResponseHeader('X-DAS-Capabilties');
            if (caps) {
                alert(caps);
            }
        } */

        var features = new Array();
        var segmentMap = {};

        var segs = responseXML.getElementsByTagName('SEGMENT');
        for (var si = 0; si < segs.length; ++si) {
            var segmentXML = segs[si];
            var segmentID = segmentXML.getAttribute('id');
            segmentMap[segmentID] = {
                min: segmentXML.getAttribute('start'),
                max: segmentXML.getAttribute('stop')
            };
            
            var featureXMLs = segmentXML.getElementsByTagName('FEATURE');
            for (var i = 0; i < featureXMLs.length; ++i) {
                var feature = featureXMLs[i];
                var dasFeature = new DASFeature();
                
                dasFeature.segment = segmentID;
                dasFeature.id = feature.getAttribute('id');
                dasFeature.label = feature.getAttribute('label');


/*
                var childNodes = feature.childNodes;
                for (var c = 0; c < childNodes.length; ++c) {
                    var cn = childNodes[c];
                    if (cn.nodeType == Node.ELEMENT_NODE) {
                        var key = cn.tagName;
                        //var val = null;
                        //if (cn.firstChild) {
                        //   val = cn.firstChild.nodeValue;
                        //}
                        dasFeature[key] = 'x';
                    }
                } */


                var spos = elementValue(feature, "START");
                var epos = elementValue(feature, "END");
                if ((spos|0) > (epos|0)) {
                    dasFeature.min = epos|0;
                    dasFeature.max = spos|0;
                } else {
                    dasFeature.min = spos|0;
                    dasFeature.max = epos|0;
                }
                {
                    var tec = feature.getElementsByTagName('TYPE');
                    if (tec.length > 0) {
                        var te = tec[0];
                        if (te.firstChild) {
                            dasFeature.type = te.firstChild.nodeValue;
                        }
                        dasFeature.typeId = te.getAttribute('id');
                        dasFeature.typeCv = te.getAttribute('cvId');
                    }
                }
                dasFeature.type = elementValue(feature, "TYPE");
                if (!dasFeature.type && dasFeature.typeId) {
                    dasFeature.type = dasFeature.typeId; // FIXME?
                }
                
                dasFeature.method = elementValue(feature, "METHOD");
                {
                    var ori = elementValue(feature, "ORIENTATION");
                    if (!ori) {
                        ori = '0';
                    }
                    dasFeature.orientation = ori;
                }
                dasFeature.score = elementValue(feature, "SCORE");
                dasFeature.links = dasLinksOf(feature);
                dasFeature.notes = dasNotesOf(feature);
                
                var groups = feature.getElementsByTagName("GROUP");
                for (var gi  = 0; gi < groups.length; ++gi) {
                    var groupXML = groups[gi];
                    var dasGroup = new DASGroup();
                    dasGroup.type = groupXML.getAttribute('type');
                    dasGroup.id = groupXML.getAttribute('id');
                    dasGroup.links = dasLinksOf(groupXML);
                    dasGroup.notes = dasNotesOf(groupXML);
                    if (!dasFeature.groups) {
                        dasFeature.groups = new Array(dasGroup);
                    } else {
                        dasFeature.groups.push(dasGroup);
                    }
                }

                // Magic notes.  Check with TAD before changing this.
                if (dasFeature.notes) {
                    for (var ni = 0; ni < dasFeature.notes.length; ++ni) {
                        var n = dasFeature.notes[ni];
                        if (n.indexOf('Genename=') == 0) {
                            var gg = new DASGroup();
                            gg.type='gene';
                            gg.id = n.substring(9);
                            if (!dasFeature.groups) {
                                dasFeature.groups = new Array(gg);
                            } else {
                                dasFeature.groups.push(gg);
                            }
                        }
                    }
                }
                
                {
                    var pec = feature.getElementsByTagName('PART');
                    if (pec.length > 0) {
                        var parts = [];
                        for (var pi = 0; pi < pec.length; ++pi) {
                            parts.push(pec[pi].getAttribute('id'));
                        }
                        dasFeature.parts = parts;
                    }
                }
                {
                    var pec = feature.getElementsByTagName('PARENT');
                    if (pec.length > 0) {
                        var parents = [];
                        for (var pi = 0; pi < pec.length; ++pi) {
                            parents.push(pec[pi].getAttribute('id'));
                        }
                        dasFeature.parents = parents;
                    }
                }
                
                features.push(dasFeature);
            }
        }
                
        callback(features, undefined, segmentMap);
    });
}

function DASAlignment(type) {
    this.type = type;
    this.objects = {};
    this.blocks = [];
}

DASSource.prototype.alignments = function(segment, options, callback) {
    var dasURI = this.uri + 'alignment?query=' + segment;
    this.doCrossDomainRequest(dasURI, function(responseXML) {
        if (!responseXML) {
            callback([], 'Failed request ' + dasURI);
            return;
        }

        var alignments = [];
        var aliXMLs = responseXML.getElementsByTagName('alignment');
        for (var ai = 0; ai < aliXMLs.length; ++ai) {
            var aliXML = aliXMLs[ai];
            var ali = new DASAlignment(aliXML.getAttribute('alignType'));
            var objXMLs = aliXML.getElementsByTagName('alignObject');
            for (var oi = 0; oi < objXMLs.length; ++oi) {
                var objXML = objXMLs[oi];
                var obj = {
                    id:          objXML.getAttribute('intObjectId'),
                    accession:   objXML.getAttribute('dbAccessionId'),
                    version:     objXML.getAttribute('objectVersion'),
                    dbSource:    objXML.getAttribute('dbSource'),
                    dbVersion:   objXML.getAttribute('dbVersion')
                };
                ali.objects[obj.id] = obj;
            }
            
            var blockXMLs = aliXML.getElementsByTagName('block');
            for (var bi = 0; bi < blockXMLs.length; ++bi) {
                var blockXML = blockXMLs[bi];
                var block = {
                    order:      blockXML.getAttribute('blockOrder'),
                    segments:   []
                };
                var segXMLs = blockXML.getElementsByTagName('segment');
                for (var si = 0; si < segXMLs.length; ++si) {
                    var segXML = segXMLs[si];
                    var seg = {
                        object:      segXML.getAttribute('intObjectId'),
                        min:         segXML.getAttribute('start'),
                        max:         segXML.getAttribute('end'),
                        strand:      segXML.getAttribute('strand'),
                        cigar:       elementValue(segXML, 'cigar')
                    };
                    block.segments.push(seg);
                }
                ali.blocks.push(block);
            }       
                    
            alignments.push(ali);
        }
        callback(alignments);
    });
}


function DASStylesheet() {
/*
    this.highZoomStyles = new Object();
    this.mediumZoomStyles = new Object();
    this.lowZoomStyles = new Object();
*/

    this.styles = [];
}

DASStylesheet.prototype.pushStyle = function(filters, zoom, style) {
    /*

    if (!zoom) {
        this.highZoomStyles[type] = style;
        this.mediumZoomStyles[type] = style;
        this.lowZoomStyles[type] = style;
    } else if (zoom == 'high') {
        this.highZoomStyles[type] = style;
    } else if (zoom == 'medium') {
        this.mediumZoomStyles[type] = style;
    } else if (zoom == 'low') {
        this.lowZoomStyles[type] = style;
    }

    */

    if (!filters) {
        filters = {type: 'default'};
    }
    var styleHolder = shallowCopy(filters);
    if (zoom) {
        styleHolder.zoom = zoom;
    }
    styleHolder.style = style;
    this.styles.push(styleHolder);
}

function DASStyle() {
}

DASSource.prototype.stylesheet = function(successCB, failureCB) {
    var dasURI, creds = this.credentials;
    if (this.stylesheet_uri) {
        dasURI = this.stylesheet_uri;
        creds = false;
    } else {
        dasURI = this.uri + 'stylesheet';
    }

    doCrossDomainRequest(dasURI, function(responseXML) {
        if (!responseXML) {
            if (failureCB) {
                failureCB();
            } 
            return;
        }
        var stylesheet = new DASStylesheet();
        var typeXMLs = responseXML.getElementsByTagName('TYPE');
        for (var i = 0; i < typeXMLs.length; ++i) {
            var typeStyle = typeXMLs[i];
            
            var filter = {};
            filter.type = typeStyle.getAttribute('id'); // Am I right in thinking that this makes DASSTYLE XML invalid?  Ugh.
            filter.label = typeStyle.getAttribute('label');
            filter.method = typeStyle.getAttribute('method');
            var glyphXMLs = typeStyle.getElementsByTagName('GLYPH');
            for (var gi = 0; gi < glyphXMLs.length; ++gi) {
                var glyphXML = glyphXMLs[gi];
                var zoom = glyphXML.getAttribute('zoom');
                var glyph = childElementOf(glyphXML);
                var style = new DASStyle();
                style.glyph = glyph.localName;
                var child = glyph.firstChild;
        
                while (child) {
                    if (child.nodeType == Node.ELEMENT_NODE) {
                        // alert(child.localName);
                        style[child.localName] = child.firstChild.nodeValue;
                    }
                    child = child.nextSibling;
                }
                stylesheet.pushStyle(filter, zoom, style);
            }
        }
        successCB(stylesheet);
    }, creds);
}

//
// sources command
// 

function DASRegistry(uri, opts)
{
    opts = opts || {};
    this.uri = uri;
    this.opts = opts;   
}

DASRegistry.prototype.sources = function(callback, failure, opts)
{
    if (!opts) {
        opts = {};
    }

    var filters = [];
    if (opts.taxon) {
        filters.push('organism=' + opts.taxon);
    }
    if (opts.auth) {
        filters.push('authority=' + opts.auth);
    }
    if (opts.version) {
        filters.push('version=' + opts.version);
    }
    var quri = this.uri;
    if (filters.length > 0) {
        quri = quri + '?' + filters.join('&');   // '&' as a separator to hack around dasregistry.org bug.
    }

    doCrossDomainRequest(quri, function(responseXML) {
        if (!responseXML && failure) {
            failure();
            return;
        }

        var sources = [];       
        var sourceXMLs = responseXML.getElementsByTagName('SOURCE');
        for (var si = 0; si < sourceXMLs.length; ++si) {
            var sourceXML = sourceXMLs[si];
            var versionXMLs = sourceXML.getElementsByTagName('VERSION');
            if (versionXMLs.length < 1) {
                continue;
            }
            var versionXML = versionXMLs[0];

            var coordXMLs = versionXML.getElementsByTagName('COORDINATES');
            var coords = [];
            for (var ci = 0; ci < coordXMLs.length; ++ci) {
                var coordXML = coordXMLs[ci];
                var coord = new DASCoords();
                coord.auth = coordXML.getAttribute('authority');
                coord.taxon = coordXML.getAttribute('taxid');
                coord.version = coordXML.getAttribute('version');
                coords.push(coord);
            }
            
            var caps = [];
            var capXMLs = versionXML.getElementsByTagName('CAPABILITY');
            var uri;
            for (var ci = 0; ci < capXMLs.length; ++ci) {
                var capXML = capXMLs[ci];
                
                caps.push(capXML.getAttribute('type'));

                if (capXML.getAttribute('type') == 'das1:features') {
                    var fep = capXML.getAttribute('query_uri');
                    uri = fep.substring(0, fep.length - ('features'.length));
                }
            }

            var props = {};
            var propXMLs = versionXML.getElementsByTagName('PROP');
            for (var pi = 0; pi < propXMLs.length; ++pi) {
                pusho(props, propXMLs[pi].getAttribute('name'), propXMLs[pi].getAttribute('value'));
            }
            
            if (uri) {
                var source = new DASSource(uri, {
                    source_uri: sourceXML.getAttribute('uri'),
                    name:  sourceXML.getAttribute('title'),
                    desc:  sourceXML.getAttribute('description'),
                    coords: coords,
                    props: props,
                    capabilities: caps
                });
                sources.push(source);
            }
        }
        
        callback(sources);
    });
}


//
// Utility functions
//

function elementValue(element, tag)
{
    var children = element.getElementsByTagName(tag);
    if (children.length > 0 && children[0].firstChild) {
        return children[0].firstChild.nodeValue;
    } else {
        return null;
    }
}

function childElementOf(element)
{
    if (element.hasChildNodes()) {
        var child = element.firstChild;
        do {
            if (child.nodeType == Node.ELEMENT_NODE) {
                return child;
            } 
            child = child.nextSibling;
        } while (child != null);
    }
    return null;
}


function dasLinksOf(element)
{
    var links = new Array();
    var maybeLinkChilden = element.getElementsByTagName('LINK');
    for (var ci = 0; ci < maybeLinkChilden.length; ++ci) {
        var linkXML = maybeLinkChilden[ci];
        if (linkXML.parentNode == element) {
            links.push(new DASLink(linkXML.firstChild ? linkXML.firstChild.nodeValue : 'Unknown', linkXML.getAttribute('href')));
        }
    }
    
    return links;
}

function dasNotesOf(element)
{
    var notes = [];
    var maybeNotes = element.getElementsByTagName('NOTE');
    for (var ni = 0; ni < maybeNotes.length; ++ni) {
        if (maybeNotes[ni].firstChild) {
            notes.push(maybeNotes[ni].firstChild.nodeValue);
        }
    }
    return notes;
}

function doCrossDomainRequest(url, handler, credentials, custAuth) {
    // TODO: explicit error handlers?

    if (window.XDomainRequest) {
        var req = new XDomainRequest();
        req.onload = function() {
            var dom = new ActiveXObject("Microsoft.XMLDOM");
            dom.async = false;
            dom.loadXML(req.responseText);
            handler(dom);
        }
        req.open("get", url);
        req.send('');
    } else {
        var reqStart = Date.now();
        var req = new XMLHttpRequest();

        req.onreadystatechange = function() {
            if (req.readyState == 4) {
              if (req.status >= 200 || req.status == 0) {
                  handler(req.responseXML, req);
              }
            }
        };
        req.open("get", url, true);
        if (credentials) {
            req.withCredentials = true;
        }
        if (custAuth) {
            req.setRequestHeader('X-DAS-Authorisation', custAuth);
        }
        req.setRequestHeader('Accept', 'application/xml,*/*');
        req.send('');
    }
}

DASSource.prototype.doCrossDomainRequest = function(url, handler) {
    var custAuth;
    if (this.xUser) {
        custAuth = 'Basic ' + btoa(this.xUser + ':' + this.xPass);
    }
    return doCrossDomainRequest(url, handler, this.credentials, custAuth);
}
/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2010
//
// domui.js: SVG UI components
//

Browser.prototype.removeAllPopups = function() {
    removeChildren(this.hPopupHolder);
    removeChildren(this.popupHolder);
}

Browser.prototype.makeTooltip = function(ele, text)
{
    var isin = false;
    var thisB = this;
    var timer = null;
    var outlistener;
    outlistener = function(ev) {
        isin = false;
        if (timer) {
            clearTimeout(timer);
            timer = null;
        }
        ele.removeEventListener('mouseout', outlistener, false);
    };

    var setup;
    setup = function(ev) {
        var mx = ev.clientX + window.scrollX, my = ev.clientY + window.scrollY;
        if (!timer) {
            timer = setTimeout(function() {
                var popup = makeElement('div',
                    [makeElement('div', null, {className: 'tooltip-arrow'}),
                     makeElement('div', text, {className: 'tooltip-inner'})], 
                    {className: 'tooltip bottom in'}, {
                    display: 'block',
                    top: '' + (my + 20) + 'px',
                    left: '' + Math.max(mx - 30, 20) + 'px'
                });
                thisB.hPopupHolder.appendChild(popup);
                var moveHandler;
                moveHandler = function(ev) {
                    try {
                        thisB.hPopupHolder.removeChild(popup);
                    } catch (e) {
                        // May have been removed by other code which clears the popup layer.
                    }
                    window.removeEventListener('mousemove', moveHandler, false);
                    if (isin) {
                        if (ele.offsetParent == null) {
                            // dlog('Null parent...');
                        } else {
                            setup(ev);
                        }
                    }
                }
                window.addEventListener('mousemove', moveHandler, false);
                timer = null;
            }, 1000);
        }
    };

    ele.addEventListener('mouseover', function(ev) {
        isin = true
        ele.addEventListener('mouseout', outlistener, false);
        setup(ev);
    }, false);
    ele.addEventListener('DOMNodeRemovedFromDocument', function(ev) {
        isin = false;
        if (timer) {
            clearTimeout(timer);
            timer = null;
        }
    }, false);
}

Browser.prototype.popit = function(ev, name, ele, opts)
{
    var thisB = this;
    if (!opts) 
        opts = {};
    if (!ev) 
        ev = {};

    var width = opts.width || 200;

    var mx, my;

    if (ev.clientX) {
        var mx =  ev.clientX, my = ev.clientY;
    } else {
        mx = 500; my= 50;
    }
    mx +=  document.documentElement.scrollLeft || document.body.scrollLeft;
    my +=  document.documentElement.scrollTop || document.body.scrollTop;
    var winWidth = window.innerWidth;

    var top = (my + 20);
    var left = Math.min(mx - (width/2), (winWidth - width - 30));

    var popup = makeElement('div');
    popup.className = 'popover fade ' + (ev.clientX ? 'bottom ' : '') + 'in';
    popup.style.display = 'block';
    popup.style.position = 'absolute';
    popup.style.top = '' + top + 'px';
    popup.style.left = '' + left + 'px';
    popup.style.width = width + 'px';
    if (width > 276) {
        // HACK Bootstrappification...
        popup.style.maxWidth = width + 'px';
    }

    popup.appendChild(makeElement('div', null, {className: 'arrow'}));

    if (name) {
        var closeButton = makeElement('button', '', {className: 'close'});
        closeButton.innerHTML = '&times;'

        closeButton.addEventListener('mouseover', function(ev) {
            closeButton.style.color = 'red';
        }, false);
        closeButton.addEventListener('mouseout', function(ev) {
            closeButton.style.color = 'black';
        }, false);
        closeButton.addEventListener('click', function(ev) {
            ev.preventDefault(); ev.stopPropagation();
            thisB.removeAllPopups();
        }, false);
        var tbar = makeElement('h4', [makeElement('span', name, null, {maxWidth: '200px'}), closeButton], {/*className: 'popover-title' */}, {paddingLeft: '10px', paddingRight: '10px'});

        var dragOX, dragOY;
        var moveHandler, upHandler;
        moveHandler = function(ev) {
            ev.stopPropagation(); ev.preventDefault();
            left = left + (ev.clientX - dragOX);
            if (left < 8) {
                left = 8;
            } if (left > (winWidth - width - 32)) {
                left = (winWidth - width - 26);
            }
            top = top + (ev.clientY - dragOY);
            top = Math.max(10, top);
            popup.style.top = '' + top + 'px';
            popup.style.left = '' + Math.min(left, (winWidth - width - 10)) + 'px';
            dragOX = ev.clientX; dragOY = ev.clientY;
        }
        upHandler = function(ev) {
            ev.stopPropagation(); ev.preventDefault();
            window.removeEventListener('mousemove', moveHandler, false);
            window.removeEventListener('mouseup', upHandler, false);
        }
        tbar.addEventListener('mousedown', function(ev) {
            ev.preventDefault(); ev.stopPropagation();
            dragOX = ev.clientX; dragOY = ev.clientY;
            window.addEventListener('mousemove', moveHandler, false);
            window.addEventListener('mouseup', upHandler, false);
        }, false);
                              

        popup.appendChild(tbar);
    }

    popup.appendChild(makeElement('div', ele, {className: 'popover-content'}, {
        padding: '0px'
    }));
    this.hPopupHolder.appendChild(popup);

    var popupHandle = {
        node: popup,
        displayed: true
    };
    popup.addEventListener('DOMNodeRemoved', function(ev) {
        if (ev.target == popup) {
            popupHandle.displayed = false;
        }
    }, false);
    return popupHandle;
}

function makeTreeTableSection(title, content, visible) {
    var ttButton = makeElement('i');
    function update() {
        if (visible) {
            ttButton.className = 'icon-chevron-down';
            content.style.display = 'table';
        } else {
            ttButton.className = 'icon-chevron-right';
            content.style.display = 'none';
        }
    }
    update();

    ttButton.addEventListener('click', function(ev) {
        ev.preventDefault(); ev.stopPropagation();
        visible = !visible;
        update();
    }, false);

    var heading = makeElement('h6', [ttButton, title]);
    return makeElement('div', [heading, content]);
}

function dlog(msg) {
    console.log(msg);
}
// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2010
//
// feature-draw.js: new feature-tier renderer
//

var MIN_PADDING = 3;
var DEFAULT_SUBTIER_MAX = 25;

function isDasBooleanTrue(s) {
    s = ('' + s).toLowerCase();
    return s==='yes' || s==='true';
}

function SubTier() {
    this.glyphs = [];
    this.height = 0;
    this.quant = null;
}

SubTier.prototype.add = function(glyph) {
    this.glyphs.push(glyph);
    this.height = Math.max(this.height, glyph.height());
    if (glyph.quant && this.quant == null) {
	this.quant = glyph.quant;
    }
}

SubTier.prototype.hasSpaceFor = function(glyph) {
    for (var i = 0; i < this.glyphs.length; ++i) {
        var g = this.glyphs[i];
        if (g.min() <= glyph.max() && g.max() >= glyph.min()) {
            return false;
        }
    }
    return true;
}

var GLOBAL_GC;

function drawFeatureTier(tier)
{
    GLOBAL_GC = tier.viewport.getContext('2d'); // Should only be used for metrics.
    sortFeatures(tier);

    var glyphs = [];
    var specials = false;

    for (var uft in tier.ungroupedFeatures) {
        var ufl = tier.ungroupedFeatures[uft];
        // var style = styles[uft] || styles['default'];
        var style = tier.styleForFeature(ufl[0]);   // FIXME this isn't quite right...
        if (!style) continue;
        if (style.glyph == 'LINEPLOT') {
            glyphs.push(makeLineGlyph(ufl, style, tier));
            specials = true;
        } else {
            for (var pgid = 0; pgid < ufl.length; ++pgid) {
                var f = ufl[pgid];
                if (f.parts) {  // FIXME shouldn't really be needed
                    continue;
                }
                var g = glyphForFeature(f, 0, tier.styleForFeature(f), tier);
		if (g)
                    glyphs.push(g);
            }
        }
    }

    // Merge supergroups
    
    if (tier.dasSource.collapseSuperGroups && !tier.bumped) {
        for (var sg in tier.superGroups) {
            var sgg = tier.superGroups[sg];
            tier.groups[sg].type = tier.groups[sgg[0]].type;   // HACK to make styling easier in DAS1.6
            var featsByType = {};
            for (var g = 0; g < sgg.length; ++g) {
                var gf = tier.groupedFeatures[sgg[g]];
                for (var fi = 0; fi < gf.length; ++fi) {
                    var f = gf[fi];
                    pusho(featsByType, f.type, f);
                }

                if (tier.groups[sg] && !tier.groups[sg].links || tier.groups[sg].links.length == 0) {
                    tier.groups[sg].links = tier.groups[sgg[0]].links;
                }

                delete tier.groupedFeatures[sgg[g]];  // 'cos we don't want to render the unmerged version.
            }

            for (var t in featsByType) {
                var feats = featsByType[t];
                var template = feats[0];
                var loc = null;
                for (var fi = 0; fi < feats.length; ++fi) {
                    var f = feats[fi];
                    var fl = new Range(f.min, f.max);
                    if (!loc) {
                        loc = fl;
                    } else {
                        loc = union(loc, fl);
                    }
                }
                var mergedRanges = loc.ranges();
                for (var si = 0; si < mergedRanges.length; ++si) {
                    var r = mergedRanges[si];

                    // begin coverage-counting
                    var posCoverage = ((r.max()|0) - (r.min()|0) + 1) * sgg.length;
                    var actCoverage = 0;
                    for (var fi = 0; fi < feats.length; ++fi) {
                        var f = feats[fi];
                        if ((f.min|0) <= r.max() && (f.max|0) >= r.min()) {
                            var umin = Math.max(f.min|0, r.min());
                            var umax = Math.min(f.max|0, r.max());
                            actCoverage += (umax - umin + 1);
                        }
                    }
                    var visualWeight = ((1.0 * actCoverage) / posCoverage);
                    // end coverage-counting

                    var newf = new DASFeature();
                    for (k in template) {
                        newf[k] = template[k];
                    }
                    newf.min = r.min();
                    newf.max = r.max();
                    if (newf.label && sgg.length > 1) {
                        newf.label += ' (' + sgg.length + ' vars)';
                    }
                    newf.visualWeight = ((1.0 * actCoverage) / posCoverage);
                    pusho(tier.groupedFeatures, sg, newf);
                    // supergroups are already in tier.groups.
                }
            }

            delete tier.superGroups[sg]; // Do we want this?
        }       
    }

    // Glyphify groups.

    var gl = new Array();
    for (var gid in tier.groupedFeatures) {
        gl.push(gid);
    }
    gl.sort(function(g1, g2) {
        var d = tier.groupedFeatures[g1][0].score - tier.groupedFeatures[g2][0].score;
        if (d > 0) {
            return -1;
        } else if (d == 0) {
            return 0;
        } else {
            return 1;
        }
    });

    var groupGlyphs = {};
    for (var gx = 0; gx < gl.length; ++gx) {
        var gid = gl[gx];
        var g = glyphsForGroup(tier.groupedFeatures[gid], 0, tier.groups[gid], tier,
                               (tier.dasSource.collapseSuperGroups && !tier.bumped) ? 'collapsed_gene' : 'tent');
        if (g) {
	    g.group = tier.groups[gid];
            groupGlyphs[gid] = g;
        }
    }

    for (var sg in tier.superGroups) {
        var sgg = tier.superGroups[sg];
        var sgGlyphs = [];
        var sgMin = 10000000000;
        var sgMax = -10000000000;
        for (var sgi = 0; sgi < sgg.length; ++sgi) {
            var gg = groupGlyphs[sgg[sgi]];
            groupGlyphs[sgg[sgi]] = null;
            if (gg) {
                sgGlyphs.push(gg);
                sgMin = Math.min(sgMin, gg.min());
                sgMax = Math.max(sgMax, gg.max());
            }
        }
        for (var sgi = 0; sgi < sgGlyphs.length; ++sgi) {
            var gg = sgGlyphs[sgi];
            glyphs.push(new PaddedGlyph(gg, sgMin, sgMax));
        }
    }
    for (var g in groupGlyphs) {
        var gg = groupGlyphs[g];
        if (gg) {
            glyphs.push(gg);
        }
    }

    // Bumping

    var unbumpedST = new SubTier();
    var bumpedSTs = [];
    var hasBumpedFeatures = false;
    var subtierMax = tier.dasSource.subtierMax || DEFAULT_SUBTIER_MAX;
    
  GLYPH_LOOP:
    for (var i = 0; i < glyphs.length; ++i) {
        var g = glyphs[i];
        if (g.bump) {
            hasBumpedFeatures = true;
        }
        if (g.bump && (tier.bumped || tier.dasSource.collapseSuperGroups)) {       // kind-of nasty.  supergroup collapsing is different from "normal" unbumping
            for (var sti = 0; sti < bumpedSTs.length;  ++sti) {
                var st = bumpedSTs[sti];
                if (st.hasSpaceFor(g)) {
                    st.add(g);
                    continue GLYPH_LOOP;
                }
            }
            if (bumpedSTs.length >= subtierMax) {
                tier.status = 'Too many overlapping features, truncating at ' + subtierMax;
            } else {
                var st = new SubTier();
                st.add(g);
                bumpedSTs.push(st);
            }
        } else {
            unbumpedST.add(g);
        }
    }

    if (unbumpedST.glyphs.length > 0) {
        bumpedSTs = [unbumpedST].concat(bumpedSTs);
    }

    for (var sti = 0; sti < bumpedSTs.length; ++sti) {
	var st = bumpedSTs[sti];
	if (st.quant) {
	    st.glyphs.unshift(new GridGlyph(st.height));
	}
    }

    tier.subtiers = bumpedSTs;
    tier.glyphCacheOrigin = tier.browser.viewStart;
}

function formatQuantLabel(v) {
    var t = '' + v;
    var dot = t.indexOf('.');
    if (dot < 0) {
        return t;
    } else {
        var dotThreshold = 2;
        if (t.substring(0, 1) == '-') {
            ++dotThreshold;
        }

        if (dot >= dotThreshold) {
            return t.substring(0, dot);
        } else {
            return t.substring(0, dot + 2);
        }
    }
}

DasTier.prototype.paint = function() {
    var subtiers = this.subtiers;
    if (!subtiers) {
	return;
    }

    var fpw = this.viewport.width|0; // this.browser.featurePanelWidth;

    var lh = MIN_PADDING;
    for (var s = 0; s < subtiers.length; ++s) {
	lh = lh + subtiers[s].height + MIN_PADDING;
    }
    lh += 6
    this.viewport.setAttribute('height', lh);
    this.viewport.style.left = '-1000px';
    this.holder.style.height = '' + Math.max(lh, 35) + 'px';
    this.updateHeight();
    this.drawOverlay();
    this.norigin = this.browser.viewStart;

    var gc = this.viewport.getContext('2d');
    gc.fillStyle = this.background;
    gc.clearRect(0, 0, fpw, Math.max(lh, 200));
    gc.restore();

    gc.save();
    var offset = ((this.glyphCacheOrigin - this.browser.viewStart)*this.browser.scale)+1000;
    gc.translate(offset, MIN_PADDING);
   
    for (var s = 0; s < subtiers.length; ++s) {
	var quant = null;
	var glyphs = subtiers[s].glyphs;
	for (var i = 0; i < glyphs.length; ++i) {
	    var glyph = glyphs[i];
	    if (glyph.min() < fpw-offset && glyph.max() > -offset) { 
		var glyph = glyphs[i];
		glyph.draw(gc);
		if (glyph.quant) {
		    quant = glyph.quant;
		}
	    }
	}
	gc.translate(0, subtiers[s].height + MIN_PADDING);
    }
    gc.restore();

    if (quant && this.quantLeapThreshold && this.featureSource && sourceAdapterIsCapable(this.featureSource, 'quantLeap')) {
	var ry = 3 + subtiers[0].height * (1.0 - ((this.quantLeapThreshold - quant.min) / (quant.max - quant.min)));

	gc.save();
	gc.strokeStyle = 'red';
	gc.lineWidth = 0.3;
	gc.moveTo(0, ry);
	gc.lineTo(5000, ry);
	gc.stroke();
	gc.restore();
    }

    this.paintQuant();
}

DasTier.prototype.paintQuant = function() {
    var quant;
    if (this.subtiers && this.subtiers.length > 0)
	quant = this.subtiers[0].quant;

    if (quant && this.quantOverlay) {
	var h = this.viewport.height;
	var w = this.quantOverlay.width;
	this.quantOverlay.height = this.viewport.height;
	var ctx = this.quantOverlay.getContext('2d');

        ctx.fillStyle = 'white'
        ctx.globalAlpha = 0.6;

	if (this.browser.rulerLocation == 'right') {
	    ctx.fillRect(w-30, 0, 30, 20);
            ctx.fillRect(w-30, h-20, 30, 20);
	} else {
            ctx.fillRect(0, 0, 30, 20);
            ctx.fillRect(0, h-20, 30, 20);
	}
        ctx.globalAlpha = 1.0;

        ctx.strokeStyle = 'black';
        ctx.lineWidth = 1;
        ctx.beginPath();

	if (this.browser.rulerLocation == 'right') {
	    ctx.moveTo(w - 8, 3);
            ctx.lineTo(w, 3);
            ctx.lineTo(w, h-3);
            ctx.lineTo(w - 8, h - 3);
	} else {
            ctx.moveTo(8, 3);
            ctx.lineTo(0,3);
            ctx.lineTo(0,h-3);
            ctx.lineTo(8,h-3);
	}
        ctx.stroke();

        ctx.fillStyle = 'black';

	if (this.browser.rulerLocation == 'right') {
	    ctx.textAlign = 'right';
	    ctx.fillText(formatQuantLabel(quant.max), w-8, 10);
            ctx.fillText(formatQuantLabel(quant.min), w-8, h-5);
	} else {
	    ctx.textAlign = 'left';
            ctx.fillText(formatQuantLabel(quant.max), 8, 10);
            ctx.fillText(formatQuantLabel(quant.min), 8, h-5);
	}
    }
}

function glyphsForGroup(features, y, groupElement, tier, connectorType) {
    var gstyle = tier.styleForFeature(groupElement);
    var label;

    var glyphs = [];
    var strand = null;
    for (var i = 0; i < features.length; ++i) {
	var f = features[i];
	if (f.orientation && strand==null) {
            strand = f.orientation;
        }
	 if (!label && f.label) {
            label = f.label;
        }

	var style = tier.styleForFeature(f);
        if (!style) {
            continue;
        }
        if (f.parts) {  // FIXME shouldn't really be needed
            continue;
        }

	var g = glyphForFeature(f, 0, style, tier, null, true);
	if (g) {
	    glyphs.push(g);
	}
    }

    if (glyphs.length == 0)
	return null;
    
    var connector = 'flat';
    if (tier.dasSource.collapseSuperGroups && !tier.bumped) {
	if (strand === '+') {
	    connector = 'collapsed+';
	} else if (strand === '-') {
	    connector = 'collapsed-';
	}
    } else {
	if (strand === '+') {
	    connector = 'hat+';
	} else if (strand === '-') {
	    connector = 'hat-';
	}
    }

    var labelText = null;
    if (label || (gstyle && (gstyle.LABEL || gstyle.LABELS))) {  // HACK, LABELS should work.
        labelText = groupElement.label || label;
        var sg = tier.groupsToSupers[groupElement.id];
        if (sg && tier.superGroups[sg]) {    // workaround case where group and supergroup IDs match.
            //if (groupElement.id != tier.superGroups[sg][0]) {
            //    dg.label = null;
            // }
        }
    }

    var gg = new GroupGlyph(glyphs, connector);
    if (labelText) {
	if (strand === '+') {
	    labelText = '>' + labelText;
	} else if (strand === '-') {
	    labelText = '<' + labelText;
	}
	gg = new LabelledGlyph(gg, labelText);
    }
    gg.bump = true;
    return gg;
}

function glyphForFeature(feature, y, style, tier, forceHeight, noLabel)
{
    var scale = tier.browser.scale, origin = tier.browser.viewStart;
    var gtype = style.glyph || 'BOX';
    var glyph;

    var min = feature.min;
    var max = feature.max;
    var type = feature.type;
    var strand = feature.orientation;
    var score = feature.score;
    var label = feature.label;

    var minPos = (min - origin) * scale;
    var rawMaxPos = ((max - origin + 1) * scale);
    var maxPos = Math.max(rawMaxPos, minPos + 1);

    var height = tier.forceHeight || style.HEIGHT || forceHeight || 12;
    var requiredHeight = height = 1.0 * height;
    var bump = style.BUMP && isDasBooleanTrue(style.BUMP);

    var gg, quant;

    if (gtype === 'CROSS' || gtype === 'EX' || gtype === 'TRIANGLE' || gtype === 'DOT' || gtype === 'SQUARE' || gtype === 'STAR') {
	var stroke = style.FGCOLOR || 'black';
        var fill = style.BGCOLOR || 'none';
        var height = tier.forceHeight || style.HEIGHT || forceHeight || 12;
	var size = style.SIZE || height;

        requiredHeight = height = 1.0 * height;
	size = 1.0 * size;

        var mid = (minPos + maxPos)/2;
        var hh = size/2;

        var mark;
        var bMinPos = minPos, bMaxPos = maxPos;

	if (gtype === 'EX') {
	    gg = new ExGlyph(mid, size, stroke);
	} else if (gtype === 'TRIANGLE') {
	    var dir = style.DIRECTION || 'N';
	    var width = style.LINEWIDTH || size;
	    gg = new TriangleGlyph(mid, size, dir, width, stroke);
	} else if (gtype === 'DOT') {
	    gg = new DotGlyph(mid, size, stroke);
	} else if (gtype === 'SQUARE') {
	    gg = new BoxGlyph(mid - hh, 0, size, size, stroke, null);
	} else if (gtype === 'STAR') {
	    var points = 5;
	    if (style.POINTS) 
		points = style.POINTS | 0;
	    gg = new StarGlyph(mid, hh, points, stroke, null);
	} else {
	    gg = new CrossGlyph(mid, size, stroke);
	}

	if (isDasBooleanTrue(style.SCATTER)) {
	    var smin = tier.dasSource.forceMin || style.MIN || tier.currentFeaturesMinScore;
            var smax = tier.dasSource.forceMax || style.MAX || tier.currentFeaturesMaxScore;

            if (!smax) {
		if (smin < 0) {
                    smax = 0;
		} else {
                    smax = 10;
		}
            }
            if (!smin) {
		smin = 0;
            }

            if ((1.0 * score) < (1.0 *smin)) {
		score = smin;
            }
            if ((1.0 * score) > (1.0 * smax)) {
		score = smax;
            }
            var relScore = ((1.0 * score) - smin) / (smax-smin);
	    var relOrigin = (-1.0 * smin) / (smax - smin);

	    if (relScore >= relOrigin) {
		height = Math.max(1, (relScore - relOrigin) * requiredHeight);
		y = y + ((1.0 - relOrigin) * requiredHeight) - height;
	    } else {
		height = Math.max(1, (relScore - relOrigin) * requiredHeight);
		y = y + ((1.0 - relOrigin) * requiredHeight);
	    }
	    
	    quant = {min: smin, max: smax};
	    gg = new TranslatedGlyph(gg, 0, y - hh, requiredHeight);
	}
    } else if (gtype === 'HISTOGRAM' || gtype === 'GRADIENT' && score !== 'undefined') {
	var smin = tier.dasSource.forceMin || style.MIN || tier.currentFeaturesMinScore;
        var smax = tier.dasSource.forceMax || style.MAX || tier.currentFeaturesMaxScore;

        if (!smax) {
            if (smin < 0) {
                smax = 0;
            } else {
                smax = 10;
            }
        }
        if (!smin) {
            smin = 0;
        }

        if ((1.0 * score) < (1.0 *smin)) {
            score = smin;
        }
        if ((1.0 * score) > (1.0 * smax)) {
            score = smax;
        }
        var relScore = ((1.0 * score) - smin) / (smax-smin);
	var relOrigin = (-1.0 * smin) / (smax - smin);

	if (gtype === 'HISTOGRAM') {
	    if (relScore >= relOrigin) {
		height = Math.max(1, (relScore - relOrigin) * requiredHeight);
		y = y + ((1.0 - relOrigin) * requiredHeight) - height;
	    } else {
		height = Math.max(1, (relOrigin - relScore) * requiredHeight);
		y = y + ((1.0 - relOrigin) * requiredHeight);
	    }
	    quant = {min: smin, max: smax};
	}

	var stroke = style.FGCOLOR || null;
	var fill = feature.override_color || style.BGCOLOR || style.COLOR1 || 'green';
	var alpha = style.ALPHA ? (1.0 * style.ALPHA) : null;

	if (style.COLOR2) {
	    var grad = style._gradient;
	    if (!grad) {
		grad = makeGradient(50, style.COLOR1, style.COLOR2, style.COLOR3);
		style._gradient = grad;
	    }

	    var step = (relScore*grad.length)|0;
	    if (step < 0) step = 0;
	    if (step >= grad.length) step = grad.length - 1;
	    fill = grad[step];
        }

	gg = new BoxGlyph(minPos, y, (maxPos - minPos), height, fill, stroke, alpha);
    } else if (gtype === 'HIDDEN') {
	gg = new PaddedGlyph(null, minPos, maxPos);
	noLabel = true;
    } else if (gtype === 'ARROW') {
	var color = style.FGCOLOR || 'purple';
	var parallel = isDasBooleanTrue(style.PARALLEL);
	var sw = isDasBooleanTrue(style.SOUTHWEST);
	var ne = isDasBooleanTrue(style.NORTHEAST);
	gg = new ArrowGlyph(minPos, maxPos, height, color, parallel, sw, ne);
    } else if (gtype === 'ANCHORED_ARROW') {
	var stroke = style.FGCOLOR || 'none';
        var fill = style.BGCOLOR || 'green';
	gg = new AArrowGlyph(minPos, maxPos, height, fill, stroke, strand);
	gg.bump = true;
    } else if (gtype === 'SPAN') {
	var stroke = style.FGCOLOR || 'black';
	gg = new SpanGlyph(minPos, maxPos, height, stroke);
    } else if (gtype === 'LINE') {
	var stroke = style.FGCOLOR || 'black';
	var lineStyle = style.STYLE || 'solid';
	gg = new LineGlyph(minPos, maxPos, height, lineStyle, strand, stroke);
    } else if (gtype === 'PRIMERS') {
	var stroke = style.FGCOLOR || 'black';
	var fill = style.BGCOLOR || 'red';
	gg = new PrimersGlyph(minPos, maxPos, height, fill, stroke);
    } else if (gtype === 'TEXT') {
	var string = style.STRING || 'text';
	var fill = style.FGCOLOR || 'black';
	gg = new TextGlyph(minPos, maxPos, height, fill, string);
    } else if (gtype === 'TOOMANY') {
	var stroke = style.FGCOLOR || 'gray';
	var fill = style.BGCOLOR || 'orange';
	gg = new TooManyGlyph(minPos, maxPos, height, fill, stroke);
    } else if (gtype === 'POINT') {
	var height = tier.forceHeight || style.HEIGHT || 30;
	var smin = tier.dasSource.forceMin || style.MIN || tier.currentFeaturesMinScore || 0;
	var smax = tier.dasSource.forceMax || style.MAX || tier.currentFeaturesMaxScore || 10;
	var yscale = ((1.0 * height) / (smax - smin));
	var relScore = ((1.0 * score) - smin) / (smax-smin);
	var sc = ((score - (1.0*smin)) * yscale)|0;
	quant = {min: smin, max: smax};

	var fill = feature.override_color || style.FGCOLOR || style.COLOR1 || 'black';
	if (style.COLOR2) {
	    var grad = style._gradient;
	    if (!grad) {
		grad = makeGradient(50, style.COLOR1, style.COLOR2, style.COLOR3);
		style._gradient = grad;
	    }

	    var step = (relScore*grad.length)|0;
	    if (step < 0) step = 0;
	    if (step >= grad.length) step = grad.length - 1;
	    fill = grad[step];
        } 

	gg = new PointGlyph((minPos + maxPos)/2, height-sc, height, fill);
    } else if (gtype === '__SEQUENCE') {
	var refSeq = null;
	if (tier.currentSequence) {
	    var csStart = tier.currentSequence.start|0;
	    var csEnd = tier.currentSequence.end|0;
	    if (csStart < min && csEnd > max) {
		refSeq = tier.currentSequence.seq.substr(min - csStart, max - min + 1);
	    }
	}
	gg = new SequenceGlyph(minPos, maxPos, height, feature.seq, refSeq);
    } else if (gtype === '__NONE') {
	return null;
    } else /* default to BOX */ {
	var stroke = style.FGCOLOR || null;
	var fill = feature.override_color || style.BGCOLOR || style.COLOR1 || 'green';
	gg = new BoxGlyph(minPos, 0, (maxPos - minPos), height, fill, stroke);
	// gg.bump = true;
    }

    if (isDasBooleanTrue(style.LABEL) && label && !noLabel) {
	gg = new LabelledGlyph(gg, label);
    }

    if (bump) {
	gg.bump = true;
    }

    gg.feature = feature;
    if (quant) {
	gg.quant = quant;
    }

    return gg;

}


	
    

DasTier.prototype.styleForFeature = function(f) {
    var cs = f._cachedStyle;
    if (cs) {
	return cs;
    }

    var ssScale = zoomForScale(this.browser.scale);

    if (!this.stylesheet) {
        return null;
    }

    var maybe = null;
    var ss = this.stylesheet.styles;
    for (var si = 0; si < ss.length; ++si) {
        var sh = ss[si];
        if (sh.zoom && sh.zoom != ssScale) {
            continue;
        }

	var labelRE = sh._labelRE;
	if (!labelRE || !labelRE.test) {
	    labelRE = new RegExp('^' + sh.label + '$');
	    sh._labelRE = labelRE;
	}
        if (sh.label && !(labelRE.test(f.label))) {
            continue;
        }
	var methodRE = sh._methodRE;
	if (!methodRE || !methodRE.test) {
	    methodRE = new RegExp('^' + sh.method + '$');
	    sh._methodRE = methodRE;
	}
        if (sh.method && !(methodRE.test(f.method))) {
            continue;
        }
        if (sh.type) {
            if (sh.type == 'default') {
                if (!maybe) {
                    maybe = sh.style;
                }
                continue;
            } else if (sh.type != f.type) {
                continue;
            }
        }
        // perfect match.
	f._cachedStyle = sh.style;
        return sh.style;
    }
    f._cachedStyle = maybe;
    return maybe;
}

function makeLineGlyph(features, style, tier) {
    var origin = tier.browser.viewStart, scale = tier.browser.scale;
    var height = tier.forceHeight || style.HEIGHT || 30;
    var min = tier.dasSource.forceMin || style.MIN || tier.currentFeaturesMinScore || 0;
    var max = tier.dasSource.forceMax || style.MAX || tier.currentFeaturesMaxScore || 10;
    var yscale = ((1.0 * height) / (max - min));
    var width = style.LINEWIDTH || 1;
    var color = style.FGCOLOR || style.COLOR1 || 'black';

    var points = [];
    for (var fi = 0; fi < features.length; ++fi) {
        var f = features[fi];

        var px = ((((f.min|0) + (f.max|0)) / 2) - origin) * scale;
        var sc = ((f.score - (1.0*min)) * yscale)|0;
        var py = (height - sc);  // FIXME y???
        points.push(px);
	points.push(py);
    }
    var lgg = new LineGraphGlyph(points, color, height);
    lgg.quant = {min: min, max: max};
    return lgg;
}
// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2012
//
// sequence-draw.js: renderers for sequence-related data
//

var MIN_TILE = 100;
var rulerTileColors = ['black', 'white'];
var baseColors = {A: 'green', C: 'blue', G: 'black', T: 'red'};
var steps = [1,2,5];


function tileSizeForScale(scale, min)
{
    if (!min) {
        min = MIN_TILE;
    }

    function ts(p) {
        return steps[p % steps.length] * Math.pow(10, (p / steps.length)|0);
    }
    var pow = steps.length;
    while (scale * ts(pow) < min) {
        ++pow;
    }
    return ts(pow);
}

function drawSeqTier(tier, seq)
{
    var scale = tier.browser.scale, knownStart = tier.browser.viewStart - (1000/scale)|0, knownEnd = tier.browser.viewEnd + (2000/scale), currentSeqMax = tier.browser.currentSeqMax;

    var fpw = tier.viewport.width|0; 

    var height = 50;
    if (seq && seq.seq) {
	height += 25;
    }
    tier.viewport.height = height;
    tier.holder.style.height = '' + height + 'px'
    tier.updateHeight();

    var gc = tier.viewport.getContext('2d');
    gc.clearRect(0, 0, fpw, tier.viewport.height);
    gc.translate(1000,0);

    var seqTierMax = knownEnd;
    if (currentSeqMax > 0 && currentSeqMax < knownEnd) {
        seqTierMax = currentSeqMax;
    }
    var tile = tileSizeForScale(scale);
    var pos = Math.max(0, ((knownStart / tile)|0) * tile);
    
    var origin = tier.browser.viewStart;

    while (pos <= seqTierMax) {
	gc.fillStyle = ((pos / tile) % 2 == 0) ? 'white' : 'black';
	gc.strokeStyle = 'black';
	gc.fillRect((pos - origin) * scale,
		    8,
		    tile*scale,
		    3);
	gc.strokeRect((pos - origin) * scale,
		      8,
		      tile*scale,
		      3);

	gc.fillStyle = 'black';
	gc.fillText(formatLongInt(pos), ((pos - origin) * scale), 22);
	

	pos += tile;
    }

    if (seq && seq.seq) {
	for (var p = knownStart; p <= knownEnd; ++p) {
	    if (p >= seq.start && p <= seq.end) {
		var base = seq.seq.substr(p - seq.start, 1).toUpperCase();
		var color = baseColors[base];
		if (!color) {
                    color = 'gray';
		}

		gc.fillStyle = color;

		if (scale >= 8) {
		    gc.fillText(base, (p - origin) * scale, 52);
		} else {
		    gc.fillRect((p - origin) * scale, 42, scale, 12); 
		}
	    }
	}
    } 

    tier.norigin = tier.browser.viewStart;
    tier.viewport.style.left = '-1000px';
}

function svgSeqTier(tier, seq) {
    var scale = tier.browser.scale, knownStart = tier.browser.viewStart - (1000/scale)|0, knownEnd = tier.browser.viewEnd + (2000/scale), currentSeqMax = tier.browser.currentSeqMax;

    var fpw = tier.viewport.width|0; 

    var seqTierMax = knownEnd;
    if (currentSeqMax > 0 && currentSeqMax < knownEnd) {
        seqTierMax = currentSeqMax;
    }
    var tile = tileSizeForScale(scale);
    var pos = Math.max(0, ((knownStart / tile)|0) * tile);
    
    var origin = tier.browser.viewStart;

    var  g = makeElementNS(NS_SVG, 'g', [], {fontSize: '8pt'}); 
    while (pos <= seqTierMax) {
	g.appendChild(
	    makeElementNS(
		NS_SVG, 'rect',
		null,
		{x: (pos-origin)*scale,
		 y: 8,
		 width: tile*scale,
		 height: 3,
		 fill: ((pos / tile) % 2 == 0) ? 'white' : 'black',
		 stroke: 'black'}));

	g.appendChild(
	    makeElementNS(
		NS_SVG, 'text',
		formatLongInt(pos),
		{x: (pos-origin)*scale,
		 y: 28,
		 fill: 'black', stroke: 'none'}));
	
	pos += tile;
    }

    if (seq && seq.seq) {
	for (var p = knownStart; p <= knownEnd; ++p) {
	    if (p >= seq.start && p <= seq.end) {
		var base = seq.seq.substr(p - seq.start, 1).toUpperCase();
		var color = baseColors[base];
		if (!color) {
                    color = 'gray';
		}


		if (scale >= 8) {
		    // gc.fillText(base, (p - origin) * scale, 12);
		    g.appendChild(
			makeElementNS(NS_SVG, 'text', base, {
			    x: (p-origin)*scale,
			    y: 52,
			    fill: color}));
		} else {
		    g.appendChild(
			makeElementNS(NS_SVG, 'rect', null, {
			    x: (p - origin)*scale,
			    y: 42,
			    width: scale,
			    height: 12,
	                    fill: color}));

		}
	    }
	}
    } 

    return g;
}
function sortFeatures(tier)
{
    var ungroupedFeatures = {};
    var groupedFeatures = {};
    var groups = {};
    var superGroups = {};
    var groupsToSupers = {};
    var nonPositional = [];
    var minScore, maxScore;
    var fbid;

    var init_fbid = function() {
        fbid = {};
        for (var fi = 0; fi < tier.currentFeatures.length; ++fi) {
            var f = tier.currentFeatures[fi];
            if (f.id) {
                fbid[f.id] = f;
            }
        }
    };
    
    var superParentsOf = function(f) {
        // FIXME: should recur.
        var spids = [];
        if (f.parents) {
            for (var pi = 0; pi < f.parents.length; ++pi) {
                var pid = f.parents[pi];
                var p = fbid[pid];
                if (!p) {
                    continue;
                }
                // alert(p.type + ':' + p.typeCv);
                if (p.typeCv == 'SO:0000704') {
                    pushnew(spids, pid);
                }
            }
        }
        return spids;
    }


    for (var fi = 0; fi < tier.currentFeatures.length; ++fi) {
        // var f = eval('[' + miniJSONify(tier.currentFeatures[fi]) + ']')[0]; 
        var f = tier.currentFeatures[fi];
        if (f.parts) {
            continue;
        }

        if (!f.min || !f.max) {
            nonPositional.push(f);
            continue;
        }

        if (f.score && f.score != '.' && f.score != '-') {
            sc = 1.0 * f.score;
            if (!minScore || sc < minScore) {
                minScore = sc;
            }
            if (!maxScore || sc > maxScore) {
                maxScore = sc;
            }
        }

        var fGroups = [];
        var fSuperGroup = null;
        if (f.groups) {
            for (var gi = 0; gi < f.groups.length; ++gi) {
                var g = f.groups[gi];
                var gid = g.id;
                if (g.type == 'gene') {
                    // Like a super-grouper...
                    fSuperGroup = gid; 
                    groups[gid] = shallowCopy(g);
                } else if (g.type == 'translation') {
                    // have to ignore this to get sensible results from bj-e :-(.
                } else {
                    pusho(groupedFeatures, gid, f);
                    groups[gid] = shallowCopy(g);
                    fGroups.push(gid);
                }
            }
        }

        if (f.parents) {
            if (!fbid) {
                init_fbid();
            }
            for (var pi = 0; pi < f.parents.length; ++pi) {
                var pid = f.parents[pi];
                var p = fbid[pid];
                if (!p) {
                    // alert("couldn't find " + pid);
                    continue;
                }
                if (!p.parts) {
                    p.parts = [f];
                }
                pushnewo(groupedFeatures, pid, p);
                pusho(groupedFeatures, pid, f);
                
                if (!groups[pid]) {
                    groups[pid] = {
                        type: p.type,
                        id: p.id,
                        label: p.label || p.id
                    };
                }
                fGroups.push(pid);

                var sgs = superParentsOf(p);
                if (sgs.length > 0) {
                    fSuperGroup = sgs[0];
                    var sp = fbid[sgs[0]];
                    groups[sgs[0]] = {
                        type: sp.type,
                        id: sp.id,
                        label: sp.label || sp.id
                    };
                    if (!tier.dasSource.collapseSuperGroups) {
                        tier.dasSource.collapseSuperGroups = true;
                    }
                }
            }   
        }

        if (fGroups.length == 0) {
            pusho(ungroupedFeatures, f.type, f);
        } else if (fSuperGroup) {
            for (var g = 0; g < fGroups.length; ++g) {
                var gid = fGroups[g];
                pushnewo(superGroups, fSuperGroup, gid);
                groupsToSupers[gid] = fSuperGroup;
            } 
        }       
    }

    tier.ungroupedFeatures = ungroupedFeatures;
    tier.groupedFeatures = groupedFeatures;
    tier.groups = groups;
    tier.superGroups = superGroups;
    tier.groupsToSupers = groupsToSupers;

    if (minScore) {
        if (minScore > 0) {
            minScore = 0;
        } else if (maxScore < 0) {
            maxScore = 0;
        }
        tier.currentFeaturesMinScore = minScore;
        tier.currentFeaturesMaxScore = maxScore;
    }
}
/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2011
//
// feature-popup.js
//

var TAGVAL_NOTE_RE = new RegExp('^([A-Za-z]+)=(.+)');

Browser.prototype.addFeatureInfoPlugin = function(handler) {
    if (!this.featureInfoPlugins) {
        this.featureInfoPlugins = [];
    }
    this.featureInfoPlugins.push(handler);
}

function FeatureInfo(hit, feature, group) {
    var name = pick(group.type, feature.type);
    var fid = pick(group.label, feature.label, group.id, feature.id);
    if (fid && fid.indexOf('__dazzle') != 0) {
        name = name + ': ' + fid;
    }

    this.hit = hit;
    this.feature = feature;
    this.group = group;
    this.title = name;
    this.sections = [];
}

FeatureInfo.prototype.setTitle = function(t) {
    this.title = t;
}

FeatureInfo.prototype.add = function(label, info) {
    if (typeof info === 'string') {
        info = makeElement('span', info);
    }
    this.sections.push({label: label, info: info});
}

Browser.prototype.featurePopup = function(ev, __ignored_feature, hit, tier) {
    var hi = hit.length;
    var feature = --hi >= 0 ? hit[hi] : {};
    var group = --hi >= 0 ? hit[hi] : {};

    var featureInfo = new FeatureInfo(hit, feature, group);
    var fips = this.featureInfoPlugins || [];
    for (fipi = 0; fipi < fips.length; ++fipi) {
        try {
            fips[fipi](feature, featureInfo);
        } catch (e) {
            console.log(e.stack || e);
        }
    }
    fips = tier.featureInfoPlugins || [];
    for (fipi = 0; fipi < fips.length; ++fipi) {
        try {
            fips[fipi](feature, featureInfo);
        } catch (e) {
            console.log(e.stack || e);
        }
    }

    this.removeAllPopups();

    var table = makeElement('table', null, {className: 'table table-striped table-condensed'});
    table.style.width = '100%';
    table.style.margin = '0px';

    var idx = 0;
    if (feature.method) {
        var row = makeElement('tr', [
            makeElement('th', 'Method'),
            makeElement('td', feature.method)
        ]);
        table.appendChild(row);
        ++idx;
    }
    {
        var loc;
        if (group.segment) {
            loc = group;
        } else {
            loc = feature;
        }
        var row = makeElement('tr', [
            makeElement('th', 'Location'),
            makeElement('td', loc.segment + ':' + loc.min + '-' + loc.max)
        ]);
        row.style.backgroundColor = this.tierBackgroundColors[idx % this.tierBackgroundColors.length];
        table.appendChild(row);
        ++idx;
    }
    if (feature.score !== undefined && feature.score !== null && feature.score != '-') {
        var row = makeElement('tr', [
            makeElement('th', 'Score'),
            makeElement('td', '' + feature.score)
        ]);
        table.appendChild(row);
        ++idx;
    }
    {
        var links = maybeConcat(group.links, feature.links);
        if (links && links.length > 0) {
            var row = makeElement('tr', [
                makeElement('th', 'Links'),
                makeElement('td', links.map(function(l) {
                    return makeElement('div', makeElement('a', l.desc, {href: l.uri, target: '_new'}));
                }))
            ]);
            table.appendChild(row);
            ++idx;
        }
    }
    {
        var notes = maybeConcat(group.notes, feature.notes);
        for (var ni = 0; ni < notes.length; ++ni) {
            var k = 'Note';
            var v = notes[ni];
            var m = v.match(TAGVAL_NOTE_RE);
            if (m) {
                k = m[1];
                v = m[2];
            }

            var row = makeElement('tr', [
                makeElement('th', k),
                makeElement('td', v)
            ]);
            table.appendChild(row);
            ++idx;
        }
    }

    for (var fisi = 0; fisi < featureInfo.sections.length; ++fisi) {
        var section = featureInfo.sections[fisi];
        table.appendChild(makeElement('tr', [
            makeElement('th', section.label),
            makeElement('td', section.info)]));
    }        

    this.popit(ev, featureInfo.title, table, {width: 400});
}
/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2010
//
// karyoscape.js
//

function Karyoscape(browser, dsn)
{
    this.browser = browser; // for tooltips.
    this.dsn = dsn;
    this.svg = makeElementNS(NS_SVG, 'g');
    this.width = 250;
}

Karyoscape.prototype.update = function(chr, start, end) {
    this.start = start;
    this.end = end;
    if (!this.chr || chr != this.chr) {
        this.chr = chr;
        removeChildren(this.svg);

        var kscape = this;
        this.dsn.features(
            new DASSegment(chr),
            {type: 'karyotype'},
            function(karyos, err, segmentMap) {
                if (segmentMap && segmentMap[chr] && segmentMap[chr].max) {
                    kscape.chrLen = segmentMap[chr].max;
                } else {
                    kscape.chrLen = null;
                }
                kscape.karyos = karyos || [];
                kscape.redraw();
            }
        );
    } else {
        this.setThumb();
    }
}

var karyo_palette = {
    gneg: 'white',
    gpos25: 'rgb(200,200,200)',
    gpos33: 'rgb(180,180,180)',
    gpos50: 'rgb(128,128,128)',
    gpos66: 'rgb(100,100,100)',
    gpos75: 'rgb(64,64,64)',
    gpos100: 'rgb(0,0,0)',
    gpos: 'rgb(0,0,0)',
    gvar: 'rgb(100,100,100)',
    acen: 'rgb(100,100,100)',
    stalk: 'rgb(100,100,100)'
};

Karyoscape.prototype.redraw = function() {
    removeChildren(this.svg);
    this.karyos = this.karyos.sort(function(k1, k2) {
        return (k1.min|0) - (k2.min|0);
    });
    if (this.karyos.length > 0) {
        if (!this.chrLen) {
            this.chrLen = this.karyos[this.karyos.length - 1].max;
        }
    } else {
        if (!this.chrLen) {
            alert('Warning: insufficient data to set up spatial navigator');
            this.chrLen = 200000000;
        } 
        this.karyos.push({
            min: 1,
            max: this.chrLen,
            label: 'gneg'
        });
    }
    var bandspans = null;
    for (var i = 0; i < this.karyos.length; ++i) {
        var k = this.karyos[i];
        var bmin = ((1.0 * k.min) / this.chrLen) * this.width;
        var bmax = ((1.0 * k.max) / this.chrLen) * this.width;
        var col = karyo_palette[k.label];
        if (!col) {
            // alert("don't understand " + k.label);
        } else {
            if (bmax > bmin) {
                var band = makeElementNS(NS_SVG, 'rect', null, {
                    x: bmin,
                    y: (k.label == 'stalk' || k.label == 'acen' ? 5 : 0),
                    width: (bmax - bmin),
                    height: (k.label == 'stalk' || k.label == 'acen'? 5 : 15),
                    stroke: 'none',
                    fill: col
                });
                if (k.label.substring(0, 1) == 'g') {
                    var br = new Range(k.min, k.max);
                    if (bandspans == null) {
                        bandspans = br;
                    } else {
                        bandspans = union(bandspans, br);
                    }
                }
                this.browser.makeTooltip(band, k.id);
                this.svg.appendChild(band);
            }
        }
    }

    if (bandspans) {
        var r = bandspans.ranges();

        var pathopsT = 'M 0 10 L 0 0';
        var pathopsB = 'M 0 5 L 0 15';
        
        var curx = 0;
        for (var ri = 0; ri < r.length; ++ri) {
            var rr = r[ri];
            var bmin = ((1.0 * rr.min()) / this.chrLen) * this.width;
            var bmax = ((1.0 * rr.max()) / this.chrLen) * this.width;
            if ((bmin - curx > 0.75)) {
                pathopsT += ' M ' + bmin + ' 0';
                pathopsB += ' M ' + bmin + ' 15';
            }
            pathopsT +=  ' L ' + bmax + ' 0';
            pathopsB +=  ' L ' + bmax + ' 15';
            curx = bmax;
        }
        if ((this.width - curx) > 0.75) {
            pathopsT += ' M ' + this.width + ' 0';
            pathopsB += ' M ' + this.width + ' 15';
        } else {
            pathopsT += ' L ' + this.width + ' 0';
            pathopsB += ' L ' + this.width + ' 15';
        }
        pathopsT +=  ' L ' + this.width + ' 10';
        pathopsB +=  ' L ' + this.width + ' 5';
        this.svg.appendChild(makeElementNS(NS_SVG, 'path', null, {
            d: pathopsT + ' ' + pathopsB,
            stroke: 'black',
            strokeWidth: 2,
            fill: 'none'
        }));
    }

    this.thumb = makeElementNS(NS_SVG, 'rect', null, {
        x: 50, y: -5, width: 8, height: 25,
        fill: 'blue', fillOpacity: 0.5, stroke: 'none'
    });
    this.svg.appendChild(this.thumb);
    this.setThumb();

    var thisKaryo = this;
    var sliderDeltaX;

    var moveHandler = function(ev) {
        ev.stopPropagation(); ev.preventDefault();
        var sliderX = Math.max(-4, Math.min(ev.clientX + sliderDeltaX, thisKaryo.width - 4));
        thisKaryo.thumb.setAttribute('x', sliderX);
//      if (thisSlider.onchange) {
//          thisSlider.onchange(value, false);
//      }
    }
    var upHandler = function(ev) {
        ev.stopPropagation(); ev.preventDefault();
        if (thisKaryo.onchange) {
            thisKaryo.onchange((1.0 * ((thisKaryo.thumb.getAttribute('x')|0) + 4)) / thisKaryo.width, true);
        }
        document.removeEventListener('mousemove', moveHandler, true);
        document.removeEventListener('mouseup', upHandler, true);
    }

    this.thumb.addEventListener('mousedown', function(ev) {
        ev.stopPropagation(); ev.preventDefault();
        sliderDeltaX = thisKaryo.thumb.getAttribute('x') - ev.clientX;
        document.addEventListener('mousemove', moveHandler, true);
        document.addEventListener('mouseup', upHandler, true);
    }, false);
}

Karyoscape.prototype.setThumb = function() {
    var pos = ((this.start|0) + (this.end|0)) / 2
    var gpos = ((1.0 * pos)/this.chrLen) * this.width;
    if (this.thumb) {
        this.thumb.setAttribute('x', gpos - 4);
    }
}
            



function FetchPool() {
    this.reqs = [];
}

FetchPool.prototype.addRequest = function(xhr) {
    this.reqs.push(xhr);
}

FetchPool.prototype.abortAll = function() {
    for (var i = 0; i < this.reqs.length; ++i) {
        this.reqs[i].abort();
    }
}

function KSCacheBaton(chr, min, max, scale, features, status) {
    this.chr = chr;
    this.min = min;
    this.max = max;
    this.scale = scale;
    this.features = features || [];
    this.status = status;
}

KSCacheBaton.prototype.toString = function() {
    return this.chr + ":" + this.min + ".." + this.max + ";scale=" + this.scale;
}

function KnownSpace(tierMap, chr, min, max, scale, seqSource) {
    this.tierMap = tierMap;
    this.chr = chr;
    this.min = min;
    this.max = max;
    this.scale = scale;
    this.seqSource = seqSource || new DummySequenceSource();

    this.featureCache = {};
}

KnownSpace.prototype.bestCacheOverlapping = function(chr, min, max) {
    var baton = this.featureCache[this.tierMap[0]];
    if (baton) {
        return baton;
    } else {
        return null;
    }
}

KnownSpace.prototype.viewFeatures = function(chr, min, max, scale) {
    // dlog('viewFeatures(' + chr + ', ' + min + ', ' + max + ', ' + scale +')');
    if (scale != scale) {
        throw "viewFeatures called with silly scale";
    }

    if (chr != this.chr) {
        throw "Can't extend Known Space to a new chromosome";
    }
    if (min < 1) {
        min = 1;
    }

    this.min = min;
    this.max = max;
    this.scale = scale;

    if (this.pool) {
        this.pool.abortAll();
    }
    this.pool = new FetchPool();
    this.awaitedSeq = new Awaited();
    this.seqWasFetched = false;
    
    this.startFetchesForTiers(this.tierMap);
}
    
function filterFeatures(features, min, max) {
    var ff = [];
    featuresByGroup = {};

    for (var fi = 0; fi < features.length; ++fi) {
        var f = features[fi];
        if (!f.min || !f.max) {
            ff.push(f);
        } else if (f.groups && f.groups.length > 0) {
            pusho(featuresByGroup, f.groups[0].id, f);
        } else if (f.min <= max && f.max >= min) {
            ff.push(f);
        }
    }

    for (var gid in featuresByGroup) {
        var gf = featuresByGroup[gid];
        var gmin = 100000000000, gmax = -100000000000;
        for (var fi = 0; fi < gf.length; ++fi) {
            var f = gf[fi];
            gmin = Math.min(gmin, f.min);
            gmax = Math.max(gmax, f.max);
        }
        if (gmin <= max || gmax >= min) {
            for (var fi = 0; fi < gf.length; ++fi) {
                ff.push(gf[fi]);
            }
        }
    }

    return ff;
}

KnownSpace.prototype.invalidate = function(tier) {
    this.featureCache[tier] = null;
    this.startFetchesForTiers([tier]);
}

KnownSpace.prototype.startFetchesForTiers = function(tiers) {
    var thisB = this;

    var awaitedSeq = this.awaitedSeq;
    var needSeq = false;

    for (var t = 0; t < tiers.length; ++t) {
        try {
            if (this.startFetchesFor(tiers[t], awaitedSeq)) {
                needSeq = true;
            }
        } catch (ex) {
            console.log('Error fetching tier source');
            console.log(ex.stack);
        }
    }

    if (needSeq && !this.seqWasFetched) {
        this.seqWasFetched = true;
        // dlog('needSeq ' + this.chr + ':' + this.min + '..' + this.max);
        var smin = this.min, smax = this.max;

        if (this.cs) {
            if (this.cs.start <= smin && this.cs.end >= smax) {
                var cachedSeq;
                if (this.cs.start == smin && this.cs.end == smax) {
                    cachedSeq = this.cs;
                } else {
                    cachedSeq = new DASSequence(this.cs.name, smin, smax, this.cs.alphabet, 
                                                this.cs.seq.substring(smin - this.cs.start, smax + 1 - this.cs.start));
                }
                return awaitedSeq.provide(cachedSeq);
            }
        }
        
        this.seqSource.fetch(this.chr, smin, smax, this.pool, function(err, seq) {
            if (seq) {
                if (!thisB.cs || (smin <= thisB.cs.start && smax >= thisB.cs.end) || 
                    (smin >= thisB.cs.end) || (smax <= thisB.cs.start) || 
                    ((smax - smin) > (thisB.cs.end - thisB.cs.start))) 
                {
                    thisB.cs = seq;
                }
                awaitedSeq.provide(seq);
            } else {
                dlog('Noseq: ' + miniJSONify(err));
                awaitedSeq.provide(null);
            }
        });
    } 
}

KnownSpace.prototype.startFetchesFor = function(tier, awaitedSeq) {
    var thisB = this;

    var source = tier.getSource() || new DummyFeatureSource();
    var needsSeq = tier.needsSequence(this.scale);
    var baton = thisB.featureCache[tier];
    var wantedTypes = tier.getDesiredTypes(this.scale);
    if (wantedTypes === undefined) {
//         dlog('skipping because wantedTypes is undef');
        return false;
    }
    if (baton) {
//      dlog('considering cached features: ' + baton);
    }
    if (baton && baton.chr === this.chr && baton.min <= this.min && baton.max >= this.max) {
        var cachedFeatures = baton.features;
        if (baton.min < this.min || baton.max > this.max) {
            cachedFeatures = filterFeatures(cachedFeatures, this.min, this.max);
        }
        
        // dlog('cached scale=' + baton.scale + '; wanted scale=' + thisB.scale);
//      if ((baton.scale < (thisB.scale/2) && cachedFeatures.length > 200) || (wantedTypes && wantedTypes.length == 1 && wantedTypes.indexOf('density') >= 0) ) {
//          cachedFeatures = downsample(cachedFeatures, thisB.scale);
//      }
        // dlog('Provisioning ' + tier.toString() + ' with ' + cachedFeatures.length + ' features from cache');
//      tier.viewFeatures(baton.chr, Math.max(baton.min, this.min), Math.min(baton.max, this.max), baton.scale, cachedFeatures);   // FIXME change scale if downsampling

        thisB.provision(tier, baton.chr, Math.max(baton.min, this.min), Math.min(baton.max, this.max), baton.scale, wantedTypes, cachedFeatures, baton.status, needsSeq ? awaitedSeq : null);

        var availableScales = source.getScales();
        if (baton.scale <= this.scale || !availableScales) {
//          dlog('used cached features');
            return needsSeq;
        } else {
//          dlog('used cached features (temporarily)');
        }
    }

    source.fetch(this.chr, this.min, this.max, this.scale, wantedTypes, this.pool, function(status, features, scale) {
        if (!baton || (thisB.min < baton.min) || (thisB.max > baton.max)) {         // FIXME should be merging in some cases?
            thisB.featureCache[tier] = new KSCacheBaton(thisB.chr, thisB.min, thisB.max, scale, features, status);
        }

        //if ((scale < (thisB.scale/2) && features.length > 200) || (wantedTypes && wantedTypes.length == 1 && wantedTypes.indexOf('density') >= 0) ) {
        //    features = downsample(features, thisB.scale);
        //}
        // dlog('Provisioning ' + tier.toString() + ' with fresh features');
        //tier.viewFeatures(thisB.chr, thisB.min, thisB.max, this.scale, features);
        thisB.provision(tier, thisB.chr, thisB.min, thisB.max, scale, wantedTypes, features, status, needsSeq ? awaitedSeq : null);
    });
    return needsSeq;
}

KnownSpace.prototype.provision = function(tier, chr, min, max, actualScale, wantedTypes, features, status, awaitedSeq) {
    if (status) {
        tier.updateStatus(status);
    } else {
        var mayDownsample = false;
        var src = tier.getSource();
        while (MappedFeatureSource.prototype.isPrototypeOf(src)) {
            src = src.source;
        }
        if (BWGFeatureSource.prototype.isPrototypeOf(src) || BAMFeatureSource.prototype.isPrototypeOf(src)) {
            mayDownsample = true;
        }
        
        // console.log('features=' + features.length + '; maybe=' + mayDownsample + '; actualScale=' + actualScale + '; thisScale=' + this.scale + '; wanted=' + wantedTypes);	

        if ((actualScale < (this.scale/2) && features.length > 200 && (!src.opts || (!src.opts.forceReduction && !src.opts.noDownsample))) ||
            (mayDownsample && wantedTypes && wantedTypes.length == 1 && wantedTypes.indexOf('density') >= 0))
        {
            features = downsample(features, this.scale);
        }

        if (awaitedSeq) {
            awaitedSeq.await(function(seq) {
                tier.viewFeatures(chr, min, max, actualScale, features, seq);
            });
        } else {
            tier.viewFeatures(chr, min, max, actualScale, features);
        }
    }
}
/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2010
//
// quant-config.js: configuration of quantitatively-scaled tiers
//

var VALID_BOUND_RE = new RegExp('^-?[0-9]+(\\.[0-9]+)?$');

Browser.prototype.makeQuantConfigButton = function(quantTools, tier, ypos) {
    var thisB = this;
    quantTools.addEventListener('mousedown', function(ev) {
        ev.stopPropagation(); ev.preventDefault();
        thisB.removeAllPopups();

        var form = makeElement('table');
        var minInput = makeElement('input', '', {value: tier.min});
        form.appendChild(makeElement('tr', [makeElement('td', 'Min:'), makeElement('td', minInput)]));
        var maxInput = makeElement('input', '', {value: tier.max});
        form.appendChild(makeElement('tr', [makeElement('td', 'Max:'), makeElement('td', maxInput)]));
        
        var updateButton = makeElement('div', 'Update');
        updateButton.style.backgroundColor = 'rgb(230,230,250)';
        updateButton.style.borderStyle = 'solid';
        updateButton.style.borderColor = 'blue';
        updateButton.style.borderWidth = '3px';
        updateButton.style.padding = '2px';
        updateButton.style.margin = '10px';
        updateButton.style.width = '150px';

        updateButton.addEventListener('mousedown', function(ev) {
            ev.stopPropagation(); ev.preventDefault();

            if (!VALID_BOUND_RE.test(minInput.value)) {
                alert("Don't understand " + minInput.value);
                return;
            }
            if (!VALID_BOUND_RE.test(maxInput.value)) {
                alert("Don't understand " + maxInput.value);
                return;
            }

            tier.dasSource.forceMin = minInput.value;
            tier.dasSource.forceMax = maxInput.value;
            thisB.removeAllPopups();
            tier.draw();
            thisB.storeStatus();          // write updated limits to storage.
        }, false);

        thisB.popit(ev, 'Configure: ' + tier.dasSource.name, [form, updateButton]);
    }, false);
}
/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2010
//
// sample.js: downsampling of quantitative features
//

var __DS_SCALES = [1, 2, 5];

function ds_scale(n) {
    return __DS_SCALES[n % __DS_SCALES.length] * Math.pow(10, (n / __DS_SCALES.length)|0);
}


function DSBin(scale, min, max) {
    this.scale = scale;
    this.tot = 0;
    this.cnt = 0;
    this.hasScore = false;
    this.min = min; this.max = max;
    this.lap = 0;
    this.covered = null;
}

DSBin.prototype.score = function() {
    if (this.cnt == 0) {
        return 0;
    } else if (this.hasScore) {
        return this.tot / this.cnt;
    } else {
        return this.lap / coverage(this.covered);
    }
}

DSBin.prototype.feature = function(f) {
    if (f.score) {
        this.tot += f.score;
        this.hasScore = true
    }
    var fMin = f.min|0;
    var fMax = f.max|0;
    var lMin = Math.max(this.min, fMin);
    var lMax = Math.min(this.max, fMax);
    // dlog('f.min=' + fMin + '; f.max=' + fMax + '; lMin=' + lMin + '; lMax=' + lMax + '; lap=' + (1.0 * (lMax - lMin + 1))/(fMax - fMin + 1));
    this.lap += (1.0 * (lMax - lMin + 1));
    ++this.cnt;
    var newRange = new Range(lMin, lMax);
    if (this.covered) {
        this.covered = union(this.covered, newRange);
    } else {
        this.covered = newRange;
    }
}

function downsample(features, targetRez) {
    var beforeDS = Date.now();

    var sn = 0;
    while (ds_scale(sn + 1) < targetRez) {
        ++sn;
    }
    var scale = ds_scale(sn);

    var binTots = [];
    var maxBin = -10000000000;
    var minBin = 10000000000;
    for (var fi = 0; fi < features.length; ++fi) {
        var f = features[fi];
        if (f.groups && f.groups.length > 0) {
            // Don't downsample complex features (?)
            return features;
        }
//      if (f.score) {
            var minLap = (f.min / scale)|0;
            var maxLap = (f.max / scale)|0;
            maxBin = Math.max(maxBin, maxLap);
            minBin = Math.min(minBin, minLap);
            for (var b = minLap; b <= maxLap; ++b) {
                var bm = binTots[b];
                if (!bm) {
                    bm = new DSBin(scale, b * scale, (b + 1) * scale - 1);
                    binTots[b] = bm;
                }
                bm.feature(f);
            }
//      }
    }

    var sampledFeatures = [];
    for (var b = minBin; b <= maxBin; ++b) {
        var bm = binTots[b];
        if (bm) {
            var f = new DASFeature();
            f.segment = features[0].segment;
            f.min = (b * scale) + 1;
            f.max = (b + 1) * scale;
            f.score = bm.score();
            f.type = 'density';
            sampledFeatures.push(f);
        }
    }

    var afterDS = Date.now();
    // dlog('downsampled ' + features.length + ' -> ' + sampledFeatures.length + ' in ' + (afterDS - beforeDS) + 'ms');
    return sampledFeatures;
}
/*
 * A JavaScript implementation of the Secure Hash Algorithm, SHA-1, as defined
 * in FIPS 180-1
 * Version 2.2 Copyright Paul Johnston 2000 - 2009.
 * Other contributors: Greg Holt, Andrew Kepert, Ydnar, Lostinet
 * Distributed under the BSD License
 * See http://pajhome.org.uk/crypt/md5 for details.
 */

/*
 * Configurable variables. You may need to tweak these to be compatible with
 * the server-side, but the defaults work in most cases.
 */
var hexcase = 0;  /* hex output format. 0 - lowercase; 1 - uppercase        */
var b64pad  = ""; /* base-64 pad character. "=" for strict RFC compliance   */

/*
 * These are the functions you'll usually want to call
 * They take string arguments and return either hex or base-64 encoded strings
 */
function hex_sha1(s)    { return rstr2hex(rstr_sha1(str2rstr_utf8(s))); }
function b64_sha1(s)    { return rstr2b64(rstr_sha1(str2rstr_utf8(s))); }
function any_sha1(s, e) { return rstr2any(rstr_sha1(str2rstr_utf8(s)), e); }
function hex_hmac_sha1(k, d)
  { return rstr2hex(rstr_hmac_sha1(str2rstr_utf8(k), str2rstr_utf8(d))); }
function b64_hmac_sha1(k, d)
  { return rstr2b64(rstr_hmac_sha1(str2rstr_utf8(k), str2rstr_utf8(d))); }
function any_hmac_sha1(k, d, e)
  { return rstr2any(rstr_hmac_sha1(str2rstr_utf8(k), str2rstr_utf8(d)), e); }

/*
 * Perform a simple self-test to see if the VM is working
 */
function sha1_vm_test()
{
  return hex_sha1("abc").toLowerCase() == "a9993e364706816aba3e25717850c26c9cd0d89d";
}

/*
 * Calculate the SHA1 of a raw string
 */
function rstr_sha1(s)
{
  return binb2rstr(binb_sha1(rstr2binb(s), s.length * 8));
}

/*
 * Calculate the HMAC-SHA1 of a key and some data (raw strings)
 */
function rstr_hmac_sha1(key, data)
{
  var bkey = rstr2binb(key);
  if(bkey.length > 16) bkey = binb_sha1(bkey, key.length * 8);

  var ipad = Array(16), opad = Array(16);
  for(var i = 0; i < 16; i++)
  {
    ipad[i] = bkey[i] ^ 0x36363636;
    opad[i] = bkey[i] ^ 0x5C5C5C5C;
  }

  var hash = binb_sha1(ipad.concat(rstr2binb(data)), 512 + data.length * 8);
  return binb2rstr(binb_sha1(opad.concat(hash), 512 + 160));
}

/*
 * Convert a raw string to a hex string
 */
function rstr2hex(input)
{
  try { hexcase } catch(e) { hexcase=0; }
  var hex_tab = hexcase ? "0123456789ABCDEF" : "0123456789abcdef";
  var output = "";
  var x;
  for(var i = 0; i < input.length; i++)
  {
    x = input.charCodeAt(i);
    output += hex_tab.charAt((x >>> 4) & 0x0F)
           +  hex_tab.charAt( x        & 0x0F);
  }
  return output;
}

/*
 * Convert a raw string to a base-64 string
 */
function rstr2b64(input)
{
  try { b64pad } catch(e) { b64pad=''; }
  var tab = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/";
  var output = "";
  var len = input.length;
  for(var i = 0; i < len; i += 3)
  {
    var triplet = (input.charCodeAt(i) << 16)
                | (i + 1 < len ? input.charCodeAt(i+1) << 8 : 0)
                | (i + 2 < len ? input.charCodeAt(i+2)      : 0);
    for(var j = 0; j < 4; j++)
    {
      if(i * 8 + j * 6 > input.length * 8) output += b64pad;
      else output += tab.charAt((triplet >>> 6*(3-j)) & 0x3F);
    }
  }
  return output;
}

/*
 * Convert a raw string to an arbitrary string encoding
 */
function rstr2any(input, encoding)
{
  var divisor = encoding.length;
  var remainders = Array();
  var i, q, x, quotient;

  /* Convert to an array of 16-bit big-endian values, forming the dividend */
  var dividend = Array(Math.ceil(input.length / 2));
  for(i = 0; i < dividend.length; i++)
  {
    dividend[i] = (input.charCodeAt(i * 2) << 8) | input.charCodeAt(i * 2 + 1);
  }

  /*
   * Repeatedly perform a long division. The binary array forms the dividend,
   * the length of the encoding is the divisor. Once computed, the quotient
   * forms the dividend for the next step. We stop when the dividend is zero.
   * All remainders are stored for later use.
   */
  while(dividend.length > 0)
  {
    quotient = Array();
    x = 0;
    for(i = 0; i < dividend.length; i++)
    {
      x = (x << 16) + dividend[i];
      q = Math.floor(x / divisor);
      x -= q * divisor;
      if(quotient.length > 0 || q > 0)
        quotient[quotient.length] = q;
    }
    remainders[remainders.length] = x;
    dividend = quotient;
  }

  /* Convert the remainders to the output string */
  var output = "";
  for(i = remainders.length - 1; i >= 0; i--)
    output += encoding.charAt(remainders[i]);

  /* Append leading zero equivalents */
  var full_length = Math.ceil(input.length * 8 /
                                    (Math.log(encoding.length) / Math.log(2)))
  for(i = output.length; i < full_length; i++)
    output = encoding[0] + output;

  return output;
}

/*
 * Encode a string as utf-8.
 * For efficiency, this assumes the input is valid utf-16.
 */
function str2rstr_utf8(input)
{
  var output = "";
  var i = -1;
  var x, y;

  while(++i < input.length)
  {
    /* Decode utf-16 surrogate pairs */
    x = input.charCodeAt(i);
    y = i + 1 < input.length ? input.charCodeAt(i + 1) : 0;
    if(0xD800 <= x && x <= 0xDBFF && 0xDC00 <= y && y <= 0xDFFF)
    {
      x = 0x10000 + ((x & 0x03FF) << 10) + (y & 0x03FF);
      i++;
    }

    /* Encode output as utf-8 */
    if(x <= 0x7F)
      output += String.fromCharCode(x);
    else if(x <= 0x7FF)
      output += String.fromCharCode(0xC0 | ((x >>> 6 ) & 0x1F),
                                    0x80 | ( x         & 0x3F));
    else if(x <= 0xFFFF)
      output += String.fromCharCode(0xE0 | ((x >>> 12) & 0x0F),
                                    0x80 | ((x >>> 6 ) & 0x3F),
                                    0x80 | ( x         & 0x3F));
    else if(x <= 0x1FFFFF)
      output += String.fromCharCode(0xF0 | ((x >>> 18) & 0x07),
                                    0x80 | ((x >>> 12) & 0x3F),
                                    0x80 | ((x >>> 6 ) & 0x3F),
                                    0x80 | ( x         & 0x3F));
  }
  return output;
}

/*
 * Encode a string as utf-16
 */
function str2rstr_utf16le(input)
{
  var output = "";
  for(var i = 0; i < input.length; i++)
    output += String.fromCharCode( input.charCodeAt(i)        & 0xFF,
                                  (input.charCodeAt(i) >>> 8) & 0xFF);
  return output;
}

function str2rstr_utf16be(input)
{
  var output = "";
  for(var i = 0; i < input.length; i++)
    output += String.fromCharCode((input.charCodeAt(i) >>> 8) & 0xFF,
                                   input.charCodeAt(i)        & 0xFF);
  return output;
}

/*
 * Convert a raw string to an array of big-endian words
 * Characters >255 have their high-byte silently ignored.
 */
function rstr2binb(input)
{
  var output = Array(input.length >> 2);
  for(var i = 0; i < output.length; i++)
    output[i] = 0;
  for(var i = 0; i < input.length * 8; i += 8)
    output[i>>5] |= (input.charCodeAt(i / 8) & 0xFF) << (24 - i % 32);
  return output;
}

/*
 * Convert an array of big-endian words to a string
 */
function binb2rstr(input)
{
  var output = "";
  for(var i = 0; i < input.length * 32; i += 8)
    output += String.fromCharCode((input[i>>5] >>> (24 - i % 32)) & 0xFF);
  return output;
}

/*
 * Calculate the SHA-1 of an array of big-endian words, and a bit length
 */
function binb_sha1(x, len)
{
  /* append padding */
  x[len >> 5] |= 0x80 << (24 - len % 32);
  x[((len + 64 >> 9) << 4) + 15] = len;

  var w = Array(80);
  var a =  1732584193;
  var b = -271733879;
  var c = -1732584194;
  var d =  271733878;
  var e = -1009589776;

  for(var i = 0; i < x.length; i += 16)
  {
    var olda = a;
    var oldb = b;
    var oldc = c;
    var oldd = d;
    var olde = e;

    for(var j = 0; j < 80; j++)
    {
      if(j < 16) w[j] = x[i + j];
      else w[j] = bit_rol(w[j-3] ^ w[j-8] ^ w[j-14] ^ w[j-16], 1);
      var t = safe_add(safe_add(bit_rol(a, 5), sha1_ft(j, b, c, d)),
                       safe_add(safe_add(e, w[j]), sha1_kt(j)));
      e = d;
      d = c;
      c = bit_rol(b, 30);
      b = a;
      a = t;
    }

    a = safe_add(a, olda);
    b = safe_add(b, oldb);
    c = safe_add(c, oldc);
    d = safe_add(d, oldd);
    e = safe_add(e, olde);
  }
  return Array(a, b, c, d, e);

}

/*
 * Perform the appropriate triplet combination function for the current
 * iteration
 */
function sha1_ft(t, b, c, d)
{
  if(t < 20) return (b & c) | ((~b) & d);
  if(t < 40) return b ^ c ^ d;
  if(t < 60) return (b & c) | (b & d) | (c & d);
  return b ^ c ^ d;
}

/*
 * Determine the appropriate additive constant for the current iteration
 */
function sha1_kt(t)
{
  return (t < 20) ?  1518500249 : (t < 40) ?  1859775393 :
         (t < 60) ? -1894007588 : -899497514;
}

/*
 * Add integers, wrapping at 2^32. This uses 16-bit operations internally
 * to work around bugs in some JS interpreters.
 */
function safe_add(x, y)
{
  var lsw = (x & 0xFFFF) + (y & 0xFFFF);
  var msw = (x >> 16) + (y >> 16) + (lsw >> 16);
  return (msw << 16) | (lsw & 0xFFFF);
}

/*
 * Bitwise rotate a 32-bit number to the left.
 */
function bit_rol(num, cnt)
{
  return (num << cnt) | (num >>> (32 - cnt));
}
// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2013
//
// svg-export.js
//

Browser.prototype.saveSVG = function() {
    var b = this;
    var saveDoc = document.implementation.createDocument(NS_SVG, 'svg', null);

    var saveRoot = makeElementNS(NS_SVG, 'g', null, {
        fontFamily: 'helvetica',
	fontSize: '8pt'
    });
    saveDoc.documentElement.appendChild(saveRoot);

    var margin = 200;

    var dallianceAnchor = makeElementNS(NS_SVG, 'a',
       makeElementNS(NS_SVG, 'text', 'Graphics from Dalliance ' + VERSION, {
           x: (b.featurePanelWidth + margin + 20)/2,
           y: 30,
           strokeWidth: 0,
           fill: 'black',
           fontSize: '12pt',
	   textAnchor: 'middle',
	   fill: 'blue'
       }));
    dallianceAnchor.setAttribute('xmlns:xlink', NS_XLINK);
    dallianceAnchor.setAttribute('xlink:href', 'http://www.biodalliance.org/');
  
    saveRoot.appendChild(dallianceAnchor);
    
    var clipRect = makeElementNS(NS_SVG, 'rect', null, {
	x: margin,
	y: 50,
	width: b.featurePanelWidth,
	height: 100000
    });
    var clip = makeElementNS(NS_SVG, 'clipPath', clipRect, {id: 'featureClip'});
    saveRoot.appendChild(clip);

    var pos = 70;
    var tierHolder = makeElementNS(NS_SVG, 'g', null, {/* clipPath: 'url(#featureClip)', clipRule: 'nonzero' */});


    for (var ti = 0; ti < b.tiers.length; ++ti) {
        var tier = b.tiers[ti];
	var tierSVG = makeElementNS(NS_SVG, 'g', null, {clipPath: 'url(#featureClip)', clipRule: 'nonzero'});
	var tierLabels = makeElementNS(NS_SVG, 'g');
	var tierTopPos = pos;

	var tierBackground = makeElementNS(NS_SVG, 'rect', null, {x: 0, y: tierTopPos, width: '10000', height: 50, fill: tier.background});
	tierSVG.appendChild(tierBackground);

	if (tier.dasSource.tier_type === 'sequence') {
	    var seqTrack = svgSeqTier(tier, tier.currentSequence);
	    
	    tierSVG.appendChild(makeElementNS(NS_SVG, 'g', seqTrack, {transform: 'translate(' + (margin) + ', ' + pos + ')'}));
	    pos += 80;
	} else {
            if (!tier.subtiers) {
		continue;
            }
	
	    var offset = ((tier.glyphCacheOrigin - b.viewStart) * b.scale);
            for (var sti = 0; sti < tier.subtiers.length; ++sti) {
		var subtier = tier.subtiers[sti];
            
		var glyphElements = [];
		for (var gi = 0; gi < subtier.glyphs.length; ++gi) {
                    var glyph = subtier.glyphs[gi];
                    glyphElements.push(glyph.toSVG());
		}

		tierSVG.appendChild(makeElementNS(NS_SVG, 'g', glyphElements, {transform: 'translate(' + (margin+offset) + ', ' + pos + ')'}));

		if (subtier.quant) {
		    var q = subtier.quant;
		    var path = new SVGPath();
		    path.moveTo(margin + 5, pos);
		    path.lineTo(margin, pos);
		    path.lineTo(margin, pos + subtier.height);
		    path.lineTo(margin + 5, pos + subtier.height);
		    tierLabels.appendChild(makeElementNS(NS_SVG, 'path', null, {d: path.toPathData(), fill: 'none', stroke: 'black', strokeWidth: '2px'}));
		    tierLabels.appendChild(makeElementNS(NS_SVG, 'text', formatQuantLabel(q.max), {x: margin - 3, y: pos + 8, textAnchor: 'end'}));
		    tierLabels.appendChild(makeElementNS(NS_SVG, 'text', formatQuantLabel(q.min), {x: margin - 3, y: pos +  subtier.height - 3, textAnchor: 'end'}));
		}

		pos += subtier.height + 3;
            }
	    pos += 10;
	}

	tierLabels.appendChild(
	    makeElementNS(
		NS_SVG, 'text',
		tier.dasSource.name,
		{x: margin - 12, y: (pos+tierTopPos+5)/2, fontSize: '12pt', textAnchor: 'end'}));

	
	tierBackground.setAttribute('height', pos - tierTopPos);
	tierHolder.appendChild(makeElementNS(NS_SVG, 'g', [tierSVG, tierLabels]));
    }
    saveRoot.appendChild(tierHolder);

    saveDoc.documentElement.setAttribute('width', b.featurePanelWidth + 20 + margin);
    saveDoc.documentElement.setAttribute('height', pos + 50);

    var svgBlob = new Blob([new XMLSerializer().serializeToString(saveDoc)]);
    var fr = new FileReader();
    fr.onload = function(fre) {
        window.open('data:image/svg+xml;' + fre.target.result.substring(6), 'Dalliance graphics', 'width=' + ( b.featurePanelWidth + 20 + margin + 'px'));
    };
    fr.readAsDataURL(svgBlob);
}
/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2010
//
// spans.js: JavaScript Intset/Location port.
//

function Range(min, max)
{
    this._min = min|0;
    this._max = max|0;
}

Range.prototype.min = function() {
    return this._min;
}

Range.prototype.max = function() {
    return this._max;
}

Range.prototype.contains = function(pos) {
    return pos >= this._min && pos <= this._max;
}

Range.prototype.isContiguous = function() {
    return true;
}

Range.prototype.ranges = function() {
    return [this];
}

Range.prototype.toString = function() {
    return '[' + this._min + '-' + this._max + ']';
}

function _Compound(ranges) {
    this._ranges = ranges;
    // assert sorted?
}

_Compound.prototype.min = function() {
    return this._ranges[0].min();
}

_Compound.prototype.max = function() {
    return this._ranges[this._ranges.length - 1].max();
}

_Compound.prototype.contains = function(pos) {
    // FIXME implement bsearch if we use this much.
    for (var s = 0; s < this._ranges.length; ++s) {
        if (this._ranges[s].contains(pos)) {
            return true;
        }
    }
    return false;
}

_Compound.prototype.isContiguous = function() {
    return this._ranges.length > 1;
}

_Compound.prototype.ranges = function() {
    return this._ranges;
}

_Compound.prototype.toString = function() {
    var s = '';
    for (var r = 0; r < this._ranges.length; ++r) {
        if (r>0) {
            s = s + ',';
        }
        s = s + this._ranges[r].toString();
    }
    return s;
}

function union(s0, s1) {
    var ranges = s0.ranges().concat(s1.ranges()).sort(rangeOrder);
    var oranges = [];
    var current = ranges[0];

    for (var i = 1; i < ranges.length; ++i) {
        var nxt = ranges[i];
        if (nxt.min() > (current.max() + 1)) {
            oranges.push(current);
            current = nxt;
        } else {
            if (nxt.max() > current.max()) {
                current = new Range(current.min(), nxt.max());
            }
        }
    }
    oranges.push(current);

    if (oranges.length == 1) {
        return oranges[0];
    } else {
        return new _Compound(oranges);
    }
}

function intersection(s0, s1) {
    var r0 = s0.ranges();
    var r1 = s1.ranges();
    var l0 = r0.length, l1 = r1.length;
    var i0 = 0, i1 = 0;
    var or = [];

    while (i0 < l0 && i1 < l1) {
        var s0 = r0[i0], s1 = r1[i1];
        var lapMin = Math.max(s0.min(), s1.min());
        var lapMax = Math.min(s0.max(), s1.max());
        if (lapMax >= lapMin) {
            or.push(new Range(lapMin, lapMax));
        }
        if (s0.max() > s1.max()) {
            ++i1;
        } else {
            ++i0;
        }
    }
    
    if (or.length == 0) {
        return null; // FIXME
    } else if (or.length == 1) {
        return or[0];
    } else {
        return new _Compound(or);
    }
}

function coverage(s) {
    var tot = 0;
    var rl = s.ranges();
    for (var ri = 0; ri < rl.length; ++ri) {
        var r = rl[ri];
        tot += (r.max() - r.min() + 1);
    }
    return tot;
}



function rangeOrder(a, b)
{
    if (a.min() < b.min()) {
        return -1;
    } else if (a.min() > b.min()) {
        return 1;
    } else if (a.max() < b.max()) {
        return -1;
    } else if (b.max() > a.max()) {
        return 1;
    } else {
        return 0;
    }
}
/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2013
//
// thub.js: support for track-hub style registries
//

var THUB_STANZA_REGEXP = /\n\s*\n/;
var THUB_PARSE_REGEXP  = /(\w+) +(.+)\n?/;
var THUB_SUBGROUP_REGEXP = /subGroup[1-9]/;

function TrackHub(url) {
    this.genomes = {};
    this.url = url;
}

function TrackHubTrack() {
}

TrackHubTrack.prototype.get = function(k) {
    if (this[k])
        return this[k];
    else if (this._parent) 
        return this._parent.get(k);
}

function TrackHubDB() {
}

TrackHubDB.prototype.getTracks = function(callback) {
    var thisB = this;
    if (this._tracks) {
        return callback(this._tracks);
    } 
    
    textXHR(this.absURL, function(trackFile, err) {
        if (err) {
            return callback(null, err);
        }

        trackFile = trackFile.replace('\\\n', ' ');

        var tracks = [];
        var tracksById = {};
        stanzas = trackFile.split(THUB_STANZA_REGEXP);
        for (var s = 0; s < stanzas.length; ++s) {
            var toks = stanzas[s].split(THUB_PARSE_REGEXP);
            var track = new TrackHubTrack();
            for (var l = 0; l < toks.length - 2; l += 3) {
                var k = toks[l+1], v = toks[l+2];
                if (k.match(THUB_SUBGROUP_REGEXP)) {
                    if (!track.subgroups)
                        track.subgroups = {};
                    var sgtoks = v.split(/\s/);
                    var sgtag = sgtoks[0];
                    var sgrecord = {name: sgtoks[1], tags: [], titles: []};
                    for (var sgti = 2; sgti < sgtoks.length; ++sgti) {
                        var grp = sgtoks[sgti].split(/=/);
                        sgrecord.tags.push(grp[0]);
                        sgrecord.titles.push(grp[1]);
                    }
                    track.subgroups[sgtag] = sgrecord;
                } else if (k === 'subGroups') {
                    var sgtoks = v.split(/(\w+)=(\w+)/);
                    track.sgm = {};
                    for (var sgti = 0; sgti < sgtoks.length - 2; sgti += 3) {
                        track.sgm[sgtoks[sgti+1]] = sgtoks[sgti + 2];
                    }
                } else {
                    track[toks[l+1]] = toks[l+2];
                }
            }

            if (track.track && (track.type || track.container)) {
                tracks.push(track);
                tracksById[track.track] = track;
            }
        }
        
        var toplevels = [];
        for (var ti = 0; ti < tracks.length; ++ti) {
            var track = tracks[ti];
            var top = true;
            if (track.parent) {
                ptoks = track.parent.split(/\s+/);
                var parent = tracksById[ptoks[0]];
                if (parent) {
                    track._parent = parent;

                    if (!parent.children)
                        parent.children = [];
                    parent.children.push(track);

                    if (parent && (!parent.compositeTrack || parent.view))
                        top = false;
                    if (parent.container == 'multiWig')
                        top = false;
                }
               
            }
            if (track.compositeTrack && !track.view)
                    top = false;  // FIXME How to handle composites properly?

            if (top)
                toplevels.push(track);
        }
            
        thisB._tracks = toplevels;
        return callback(thisB._tracks, null);
    });
}

function connectTrackHub(hubURL, callback) {
    textXHR(hubURL, function(hubFile, err) {
        if (err) {
            return callback(null, err);
        }

        var toks = hubFile.split(THUB_PARSE_REGEXP);
        var hub = new TrackHub(hubURL);
        for (var l = 0; l < toks.length - 2; l += 3) {
            hub[toks[l+1]] = toks[l+2];
        }
        
        
        if (hub.genomesFile) {
            var genURL = relativeURL(hubURL, hub.genomesFile);
            textXHR(genURL, function(genFile, err) {
                if (err) {
                    return callback(null, err);
                }

                stanzas = genFile.split(THUB_STANZA_REGEXP);
                for (var s = 0; s < stanzas.length; ++s) {
                    var toks = stanzas[s].split(THUB_PARSE_REGEXP);
                    var gprops = new TrackHubDB();
                    for (var l = 0; l < toks.length - 2; l += 3) {
                        gprops[toks[l+1]] = toks[l+2];
                    }
                    if (gprops.genome && gprops.trackDb) {
                        gprops.absURL = relativeURL(genURL, gprops.trackDb);
                        hub.genomes[gprops.genome] = gprops;
                    }
                }

                callback(hub);
                        
            });
        } else {
            callback(null, 'No genomesFile');
        }
    })
}


TrackHubTrack.prototype.toDallianceSource = function() {
    var source = {
        name: this.shortLabel,
        desc: this.longLabel
    };

    if (this.container == 'multiWig') {
        source.merge = 'concat';
        source.overlay = [];
        var children = this.children || [];
        source.style = [];
        source.noDownsample = true;

        for (var ci = 0; ci < children.length; ++ci) {
            var ch = children[ci];
            var cs = ch.toDallianceSource()
            source.overlay.push(cs);

            if (cs.style) {
                for (var si = 0; si < cs.style.length; ++si) {
                    var style = cs.style[si];
                    style.method = ch.shortLabel;  // FIXME
                    if (this.aggregate == 'transparentOverlay')
                        style.style.ALPHA = 0.5;
                    source.style.push(style);
                }
            }
        }
        return source;

        
    } else {
        typeToks = this.type.split(/\s+/);
        if (typeToks[0] == 'bigBed') {
            source.bwgURI = this.bigDataUrl;
            source.style = this.bigbedStyles();
            return source;
        } else if (typeToks[0] == 'bigWig') {
            source.bwgURI = this.bigDataUrl;
            source.style = this.bigwigStyles();
            source.noDownsample = true;     // FIXME seems like a blunt instrument...
            
            if (this.yLineOnOff && this.yLineOnOff == 'on') {
                source.quantLeapThreshold = this.yLineMark !== undefined ? (1.0 * this.yLineMark) : 0.0;
            }

            return source;
        } else if (typeToks[0] == 'bam') {
            source.bamURI = this.bigDataUrl;
            return source;
        } else {
            console.log('Unsupported ' + this.type);
        }
    }
}

TrackHubTrack.prototype.bigwigStyles = function() {
    var min, max;
    if (typeToks.length >= 3) {
        min = 1.0 * typeToks[1];
        max = 1.0 * typeToks[2];
    }

    var height;
    if (this.maxHeightPixels) {
        var mhpToks = this.maxHeightPixels.split(/:/);
        if (mhpToks.length == 3) {
            height = mhpToks[1] | 0;
        } else {
            console.log('maxHeightPixels should be of the form max:default:min');
        }
    }
    
    var gtype = 'bars';
    if (this.graphTypeDefault) {
        gtype = this.graphTypeDefault;
    }
    
    var color = 'black';
    var altColor = null;
    if (this.color) {
        color = 'rgb(' + this.color + ')';
    }
    if (this.altColor) {
        altColor = 'rgb(' + this.altColor + ')';
    }
    
    var stylesheet = new DASStylesheet();
    var wigStyle = new DASStyle();
    if (gtype == 'points') {
        wigStyle.glyph = 'POINT';
    } else {
        wigStyle.glyph = 'HISTOGRAM';
    }

    if (altColor) {
        wigStyle.COLOR1 = color;
        wigStyle.COLOR2 = altColor;
    } else {
        wigStyle.BGCOLOR = color;
    }
    wigStyle.HEIGHT = height || 30;
    if (min || max) {
        wigStyle.MIN = min;
        wigStyle.MAX = max;
    }
    stylesheet.pushStyle({type: 'default'}, null, wigStyle);
    return stylesheet.styles;
}

TrackHubTrack.prototype.bigbedStyles = function() {
    var visibility = this.get('visibility') || 'full';
    var color = this.get('color');
    if (color)
        color = 'rgb(' + color + ')';
    else 
        color = 'blue';
    
    var stylesheet = new DASStylesheet();
    var wigStyle = new DASStyle();
    wigStyle.glyph = 'BOX';
    wigStyle.FGCOLOR = 'black';
    wigStyle.BGCOLOR = color;
    wigStyle.HEIGHT = (visibility == 'full' || visibility == 'pack') ? 12 : 8;
    wigStyle.BUMP = (visibility == 'full' || visibility == 'pack');
    wigStyle.LABEL = (visibility == 'full' || visibility == 'pack');
    wigStyle.ZINDEX = 20;
    stylesheet.pushStyle({type: 'bigwig'}, null, wigStyle);
    
    var tlStyle = new DASStyle();
    tlStyle.glyph = 'BOX';
    tlStyle.FGCOLOR = 'black';
    tlStyle.BGCOLOR = 'red'
    tlStyle.HEIGHT = 10;
    tlStyle.BUMP = true;
    tlStyle.ZINDEX = 20;
    stylesheet.pushStyle({type: 'bb-translation'}, null, tlStyle);
    
    var tsStyle = new DASStyle();
    tsStyle.glyph = 'BOX';
    tsStyle.FGCOLOR = 'black';
    tsStyle.BGCOLOR = 'white';
    tsStyle.HEIGHT = 10;
    tsStyle.ZINDEX = 10;
    tsStyle.BUMP = true;
    tsStyle.LABEL = true;
    stylesheet.pushStyle({type: 'bb-transcript'}, null, tsStyle);

/*
    var densStyle = new DASStyle();
    densStyle.glyph = 'HISTOGRAM';
    densStyle.COLOR1 = 'white';
    densStyle.COLOR2 = 'black';
    densStyle.HEIGHT=30;
    stylesheet.pushStyle({type: 'density'}, null, densStyle); */

    return stylesheet.styles;
}
/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2010
//
// tier.js: (try) to encapsulate the functionality of a browser tier.
//

var __tier_idSeed = 0;

function DasTier(browser, source, viewport, holder, overlay, placard, placardContent)
{
    this.id = 'tier' + (++__tier_idSeed);
    this.browser = browser;
    this.dasSource = new DASSource(source);
    this.viewport = viewport;
    this.holder = holder;
    this.overlay = overlay;
    this.placard = placard;
    this.placardContent = placardContent;
    this.req = null;
    this.layoutHeight = 25;
    this.bumped = true; 
    if (source.quantLeapThreshold) {
        this.quantLeapThreshold = source.quantLeapThreshold;
    }
    if (this.dasSource.collapseSuperGroups) {
        this.bumped = false;
    }
    this.y = 0;
    this.layoutWasDone = false;

    if (source.featureInfoPlugin) {
        this.addFeatureInfoPlugin(source.featureInfoPlugin);
    }

    this.initSources();
}

DasTier.prototype.toString = function() {
    return this.id;
}

DasTier.prototype.addFeatureInfoPlugin = function(p) {
    if (!this.featureInfoPlugins) 
        this.featureInfoPlugins = [];
    this.featureInfoPlugins.push(p);
}

DasTier.prototype.init = function() {
    var tier = this;

    if (tier.dasSource.style) {
        this.stylesheet = {styles: tier.dasSource.style};
        this.browser.refreshTier(this);
    } else {
        var ssSource;
        if (tier.dasSource.stylesheet_uri) {
            ssSource = new DASFeatureSource(tier.dasSource);
        } else {
            ssSource = tier.getSource();
        }
        tier.status = 'Fetching stylesheet';
        
        ssSource.getStyleSheet(function(ss, err) {
            if (err) {
                tier.error = 'No stylesheet';
                tier.stylesheet = new DASStylesheet();
                var defStyle = new DASStyle();
                defStyle.glyph = 'BOX';
                defStyle.BGCOLOR = 'blue';
                defStyle.FGCOLOR = 'black';
                tier.stylesheet.pushStyle({type: 'default'}, null, defStyle);
                tier.browser.refreshTier(tier);
            } else {
                tier.stylesheet = ss;
                tier.browser.refreshTier(tier);
            }
        });
    }
}

DasTier.prototype.styles = function(scale) {
    // alert('Old SS code called');
    if (this.stylesheet == null) {
        return null;
    } else if (this.browser.scale > 0.2) {
        return this.stylesheet.highZoomStyles;
    } else if (this.browser.scale > 0.01) {
        return this.stylesheet.mediumZoomStyles;
    } else {
        return this.stylesheet.lowZoomStyles;
    }
}

DasTier.prototype.getSource = function() {
    return this.featureSource;
}

DasTier.prototype.getDesiredTypes = function(scale) {
    var fetchTypes = [];
    var inclusive = false;
    var ssScale = zoomForScale(this.browser.scale);

    if (this.stylesheet) {
        // dlog('ss = ' + miniJSONify(this.stylesheet));
        var ss = this.stylesheet.styles;
        for (var si = 0; si < ss.length; ++si) {
            var sh = ss[si];
            if (!sh.zoom || sh.zoom == ssScale) {
                if (!sh.type || sh.type == 'default') {
                    inclusive = true;
                    break;
                } else {
                    pushnew(fetchTypes, sh.type);
                }
            }
        }
    } else {
        // inclusive = true;
        return undefined;
    }

    if (inclusive) {
        return null;
    } else {
        return fetchTypes;
    }
}

DasTier.prototype.needsSequence = function(scale ) {
    if (this.dasSource.tier_type === 'sequence' && scale < 5) {
        return true;
    } else if ((this.dasSource.bamURI || this.dasSource.bamBlob) && scale < 20) {
        return true
    }
    return false;
}

DasTier.prototype.setStatus = function(status) {
    dlog(status);
}

DasTier.prototype.viewFeatures = function(chr, min, max, scale, features, sequence) {
    this.currentFeatures = features;
    this.currentSequence = sequence;
    
    this.knownChr = chr;
    this.knownStart = min; this.knownEnd = max;
    this.status = null; this.error = null;

    this.draw();
}

DasTier.prototype.updateStatus = function(status) {
    if (status) {
        this.currentFeatures = [];
        this.currentSequence = null;
        this.error = status;
        this.placardContent.innerText = status;
        this.placard.style.display = 'block';
        this.holder.style.display = 'none';
    } else {
        this.placard.style.display = 'none';
        this.holder.style.display = 'block';
    }
}

DasTier.prototype.draw = function() {
    var features = this.currentFeatures;
    var seq = this.currentSequence;
    if (this.dasSource.tier_type === 'sequence') {
        drawSeqTier(this, seq); 
    } else {
        drawFeatureTier(this);
    }
    this.paint();
    this.originHaxx = 0;
    this.browser.arrangeTiers();
}

function zoomForScale(scale) {
    var ssScale;
    if (scale > 0.2) {
        ssScale = 'high';
    } else if (scale > 0.01) {
        ssScale = 'medium';
    } else  {
        ssScale = 'low';
    }
    return ssScale;
}


DasTier.prototype.findNextFeature = function(chr, pos, dir, fedge, callback) {
    if (this.quantLeapThreshold) {
        var width = this.browser.viewEnd - this.browser.viewStart + 1;
        pos = (pos +  ((width * dir) / 2))|0
        this.featureSource.quantFindNextFeature(chr, pos, dir, this.quantLeapThreshold, callback);
    } else {
        if (this.knownStart && pos >= this.knownStart && pos <= this.knownEnd) {
            if (this.currentFeatures) {
                var bestFeature = null;
                for (var fi = 0; fi < this.currentFeatures.length; ++fi) {
                    var f = this.currentFeatures[fi];
                    if (!f.min || !f.max) {
                        continue;
                    }
                    if (f.parents && f.parents.length > 0) {
                        continue;
                    }
                    if (dir < 0) {
                        if (fedge == 1 && f.max >= pos && f.min < pos) {
                            if (!bestFeature || f.min > bestFeature.min ||
                                (f.min == bestFeature.min && f.max < bestFeature.max)) {
                                bestFeature = f;
                            }
                        } else if (f.max < pos) {
                            if (!bestFeature || f.max > bestFeature.max || 
                                (f.max == bestFeature.max && f.min < bestFeature.min) ||
                                (f.min == bestFeature.mmin && bestFeature.max >= pos)) {
                                bestFeature = f;
                            } 
                        }
                    } else {
                        if (fedge == 1 && f.min <= pos && f.max > pos) {
                            if (!bestFeature || f.max < bestFeature.max ||
                                (f.max == bestFeature.max && f.min > bestFeature.min)) {
                                bestFeature = f;
                            }
                        } else if (f.min > pos) {
                            if (!bestFeature || f.min < bestFeature.min ||
                                (f.min == bestFeature.min && f.max > bestFeature.max) ||
                                (f.max == bestFeature.max && bestFeature.min <= pos)) {
                                bestFeature = f;
                            }
                        }
                    }
                }
                if (bestFeature) {
                    //                dlog('bestFeature = ' + miniJSONify(bestFeature));
                    return callback(bestFeature);
                }
                if (dir < 0) {
                    pos = this.knownStart;
                } else {
                    pos = this.knownEnd;
                }
            }
        }

        this.featureSource.findNextFeature(chr, pos, dir, callback);
    }
}


DasTier.prototype.updateLabel = function() {
   this.bumpButton.className = this.bumped ? 'icon-minus-sign' : 'icon-plus-sign';
   if (this.dasSource.collapseSuperGroups) {
        this.bumpButton.style.display = 'inline-block';
    } else {
        this.bumpButton.style.display = 'none';
    }
}

DasTier.prototype.updateHeight = function() {
    //if (this.row)
        this.row.style.height = '' + Math.max(this.holder.clientHeight, this.label.clientHeight + 4) + 'px';
 }

DasTier.prototype.drawOverlay = function() {
    var t = this;
    var b = this.browser;
    var g = t.overlay.getContext('2d');
    
    t.overlay.height = t.viewport.height;
    // g.clearRect(0, 0, t.overlay.width, t.overlay.height);
    
    var origin = b.viewStart - (1000/b.scale);
    var visStart = b.viewStart - (1000/b.scale);
    var visEnd = b.viewEnd + (1000/b.scale);


    for (var hi = 0; hi < b.highlights.length; ++hi) {
        var h = b.highlights[hi];
        if (h.chr == b.chr && h.min < visEnd && h.max > visStart) {
            g.globalAlpha = 0.3;
            g.fillStyle = 'red';
            g.fillRect((h.min - origin) * b.scale,
                       0,
                       (h.max - h.min) * b.scale,
                       t.overlay.height);
        }
    }

    t.oorigin = b.viewStart;
    t.overlay.style.left = '-1000px'
}
/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2010
//
// track-adder.js
//

function sourceURI(source) {
    // FIXME
    return source.uri || source.bwgURI || source.bamURI;
}

Browser.prototype.currentlyActive = function(source) {
    var suri = sourceURI(source);
    for (var i = 0; i < this.tiers.length; ++i) {
        var ts = this.tiers[i].dasSource;
        var tsuri = sourceURI(ts);
        if (tsuri == suri || tsuri == suri + '/') {
            // Special cases where we might meaningfully want two tiers of the same URI.
            if (ts.tier_type) {
                if (!source.tier_type || source.tier_type != ts.tier_type) {
                    continue;
                }
            }
            if (ts.stylesheet_uri) {
                if (!source.stylesheet_uri || source.stylesheet_uri != ts.stylesheet_uri) {
                    continue;
                }
            }

            return true;
        }
    }
    return false;
}

Browser.prototype.makeButton = function(name, tooltip) {
    var regButton = makeElement('a', name, {href: '#'});
    if (tooltip) {
        this.makeTooltip(regButton, tooltip);
    }
    return makeElement('li', regButton);
}

function activateButton(addModeButtons, which) {
    for (var i = 0; i < addModeButtons.length; ++i) {
        var b = addModeButtons[i];
        if (b === which) {
            b.classList.add('active');
        } else {
            b.classList.remove('active');
        }
    }
}

Browser.prototype.showTrackAdder = function(ev) {
    var thisB = this;
    var mx =  ev.clientX, my = ev.clientY;
    mx +=  document.documentElement.scrollLeft || document.body.scrollLeft;
    my +=  document.documentElement.scrollTop || document.body.scrollTop;

    var popup = makeElement('div');
    popup.appendChild(makeElement('div', null));

    var addModeButtons = [];
    var makeStab, makeStabObserver;
    var regButton = this.makeButton('Registry', 'Browse compatible datasources from the DAS registry');
    addModeButtons.push(regButton);
    
    for (var m in this.mappableSources) {
        var mf  = function(mm) {
            var mapButton = thisB.makeButton(thisB.chains[mm].srcTag, 'Browse datasources mapped from ' + thisB.chains[mm].srcTag);
            addModeButtons.push(mapButton);
            mapButton.addEventListener('click', function(ev) {
                ev.preventDefault(); ev.stopPropagation();
                activateButton(addModeButtons, mapButton);
                makeStab(thisB.mappableSources[mm], mm);
            }, false);
        }; mf(m);
    }
    

    var makeHubButton = function(hub) {
        if (thisB.coordSystem.ucscName && hub.genomes[thisB.coordSystem.ucscName]) {
            var hubRemove = makeElement('i', null, {className: 'icon-remove'});
            var hbContent = makeElement('span', [hub.shortLabel, ' ', hubRemove]);
            var hubButton = thisB.makeButton(hbContent, hub.longLabel);
            addModeButtons.push(hubButton);
            
            hubButton.addEventListener('click', function(ev) {
                ev.preventDefault(); ev.stopPropagation();

                hub.genomes[thisB.coordSystem.ucscName].getTracks(function(tracks, err) {
                    if (err) {
                        console.log(err);
                    }

                    activateButton(addModeButtons, hubButton);
                    makeHubStab(tracks);
                });
            }, false);

            hubRemove.addEventListener('click', function(ev) {
                ev.preventDefault(); ev.stopPropagation();
                
                for (var hi = 0; hi < thisB.hubs.length; ++hi) {
                    if (thisB.hubs[hi] == hub.url) {
                        console.log('index ' + hi);
                        thisB.hubs.splice(hi, 1);
                        break;
                    }
                }
                for (var hi = 0; hi < thisB.hubObjects.length; ++hi) {
                    if (thisB.hubObjects[hi].url == hub.url) {
                        thisB.hubObjects.splice(hi, 1);
                        break;
                    }
                }

                modeButtonHolder.removeChild(hubButton);
                activateButton(addModeButtons, addHubButton);
                switchToHubConnectMode();
            }, false);

            return hubButton;
        }
    }
    for (var hi = 0; hi < this.hubObjects.length; ++hi) {
        var hub = this.hubObjects[hi];
        makeHubButton(hub);
    }

    var defButton = this.makeButton('Defaults', 'Browse the default set of data for this browser');
    addModeButtons.push(defButton);
    var custButton = this.makeButton('Custom', 'Add arbitrary DAS data');
    addModeButtons.push(custButton);
    var binButton = this.makeButton('Binary', 'Add data in bigwig or bigbed format');
    addModeButtons.push(binButton);
    var addHubButton = this.makeButton('+', 'Connect to a new track-hub');
    addModeButtons.push(addHubButton);

    activateButton(addModeButtons, regButton);
    var modeButtonHolder = makeElement('ul', addModeButtons, {className: 'nav nav-tabs'}, {marginBottom: '0px'});
    popup.appendChild(modeButtonHolder);
    
    // popup.appendChild(makeElement('div', null, {}, {clear: 'both', height: '10px'})); // HACK only way I've found of adding appropriate spacing in Gecko.
    
    var addButtons = [];
    var custURL, custName, custCS, custQuant, custFile, custUser, custPass;
    var customMode = false;
    var dataToFinalize = null;

    var asform = makeElement('form', null, {}, {clear: 'both'});
    asform.addEventListener('submit', function(ev) {
            ev.stopPropagation(); ev.preventDefault();
            doAdd();
            return false;
    }, true); 
    var stabHolder = makeElement('div');
    stabHolder.style.position = 'relative';
    stabHolder.style.overflow = 'auto';
    stabHolder.style.height = '400px';
    asform.appendChild(stabHolder);

    var __mapping;
    var __sourceHolder;


    makeStab = function(msources, mapping) {
        refreshButton.style.visibility = 'visible';
        if (__sourceHolder) {
            __sourceHolder.removeListener(makeStabObserver);
        }
        __mapping = mapping;
        __sourceHolder = msources;
        __sourceHolder.addListenerAndFire(makeStabObserver);
    }

    makeStabObserver = function(msources) {
        customMode = false;
        addButtons = [];
        removeChildren(stabHolder);
        if (!msources) {
            stabHolder.appendChild(makeElement('p', 'Dalliance was unable to retrieve data source information from the DAS registry, please try again later'));
            return;
        }
        
        var stabBody = makeElement('tbody', null, {className: 'table table-striped table-condensed'});
        var stab = makeElement('table', stabBody, {className: 'table table-striped table-condensed'}, {width: '100%'}); 
        var idx = 0;

        var sources = [];
        for (var i = 0; i < msources.length; ++i) {
            sources.push(msources[i]);
        }
        
        sources.sort(function(a, b) {
            return a.name.toLowerCase().trim().localeCompare(b.name.toLowerCase().trim());
        });

        for (var i = 0; i < sources.length; ++i) {
            var source = sources[i];
            var r = makeElement('tr');

            var bd = makeElement('td');
            bd.style.textAlign = 'center';
            if (!source.props || source.props.cors) {
                var b = makeElement('input');
                b.type = 'checkbox';
                b.dalliance_source = source;
                if (__mapping) {
                    b.dalliance_mapping = __mapping;
                }
                b.checked = thisB.currentlyActive(source);
                bd.appendChild(b);
                addButtons.push(b);
                b.addEventListener('change', function(ev) {
                    if (ev.target.checked) {
                        thisB.addTier(ev.target.dalliance_source);
                    } else {
                        thisB.removeTier(ev.target.dalliance_source);
                    }
                });
            } else {
                bd.appendChild(document.createTextNode('!'));
                thisB.makeTooltip(bd, makeElement('span', ["This data source isn't accessible because it doesn't support ", makeElement('a', "CORS", {href: 'http://www.w3.org/TR/cors/'}), "."]));
            }
            r.appendChild(bd);
            var ld = makeElement('td');
            ld.appendChild(document.createTextNode(source.name));
            if (source.desc && source.desc.length > 0) {
                thisB.makeTooltip(ld, source.desc);
            }
            r.appendChild(ld);
            stabBody.appendChild(r);
            ++idx;
        }
        stabHolder.appendChild(stab);
    };

    function makeHubStab(tracks) {
        customMode = false;
        addButtons = [];
        removeChildren(stabHolder);
        
        var ttab = makeElement('div');
        var sources = [];
        for (var i = 0; i < tracks.length; ++i) {
            sources.push(tracks[i]);
        }
        
        sources.sort(function(a, b) {
            return a.shortLabel.toLowerCase().trim().localeCompare(b.shortLabel.toLowerCase().trim());
        });

        var groups = [];
        var tops = [];
        
        for (var ti = 0; ti < sources.length; ++ti) {
            var track = sources[ti];
            if (track.children && track.children.length > 0 && track.container != 'multiWig') {
                groups.push(track);
            } else {
                tops.push(track);
            }
        }
        if (tops.length > 0) {
            groups.push({
                shortLabel: 'Others',
                children: tops});
        }
        
        for (var gi = 0; gi < groups.length; ++gi) {
            var group = groups[gi];
            var dg = group;
            if (!dg.dimensions && dg._parent && dg._parent.dimensions)
                dg = dg._parent;

            var dprops = {}
            if (dg.dimensions) {
                var dtoks = dg.dimensions.split(/(\w+)=(\w+)/);
                for (var dti = 0; dti < dtoks.length - 2; dti += 3) {
                    dprops[dtoks[dti + 1]] = dtoks[dti + 2];
                }
            }

            if (dprops.dimX && dprops.dimY) {
                var dimX = dprops.dimX, dimY = dprops.dimY;
                var sgX = dg.subgroups[dimX];
                var sgY = dg.subgroups[dimY];
                
                var trks = {};
                for (var ci = 0; ci < group.children.length; ++ci) {
                    var child = group.children[ci];
                    var vX = child.sgm[dimX], vY = child.sgm[dimY];
                    if (!trks[vX])
                        trks[vX] = {};
                    trks[vX][vY] = child;
                }
                __test_trks = trks;

                var matrix = makeElement('table', null, {className: 'table table-striped table-condensed'});
                {
                    var header = makeElement('tr');
                    header.appendChild(makeElement('td'));   // blank corner element
                    for (var si = 0; si < sgX.titles.length; ++si) {
                        var h = makeElement('th', sgX.titles[si], {}, {transform: 'rotate(-45deg)', transformOrigin: '0px 0px', webkitTransform: 'rotate(-45deg)', webkitTransformOrigin: '0px 0px'});
                        header.appendChild(h);
                    }
                    matrix.appendChild(header);
                }

                var mbody = makeElement('tbody', null, {className: 'table table-striped table-condensed'})
                for (var yi = 0; yi < sgY.titles.length; ++yi) {
                    var vY = sgY.tags[yi];
                    var row = makeElement('tr');
                    row.appendChild(makeElement('th', sgY.titles[yi]));
                    
                    for (var xi = 0; xi < sgX.titles.length; ++xi) {
                        var vX = sgX.tags[xi];
                        var cell = makeElement('td');
                        if (trks[vX] && trks[vX][vY]) {
                            var track = trks[vX][vY];
                            var ds = track.toDallianceSource();
                            if (!ds)
                                continue;
                            
                            var r = makeElement('tr');
                            var bd = makeElement('td');
                            bd.style.textAlign = 'center';
                            
                            var b = makeElement('input');
                            b.type = 'checkbox';
                            b.dalliance_track = track;
                            if (__mapping) {
                                b.dalliance_mapping = __mapping;
                            }
                            b.checked = thisB.currentlyActive(ds); // FIXME!
                            cell.appendChild(b);
                            b.addEventListener('change', function(ev) {
                                if (ev.target.checked) {
                                    thisB.addTier(ev.target.dalliance_track.toDallianceSource());
                                } else {
                                    thisB.removeTier(ev.target.dalliance_track.toDallianceSource());
                                }
                            });

                        }
                        row.appendChild(cell);
                    } 
                    mbody.appendChild(row);
                }
                matrix.appendChild(mbody);
                ttab.appendChild(makeTreeTableSection(group.shortLabel, matrix, gi==0));                
            } else {
                var stabBody = makeElement('tbody', null, {className: 'table table-striped table-condensed'});
                var stab = makeElement('table', stabBody, {className: 'table table-striped table-condensed'}, {width: '100%'}); 
                var idx = 0;
            
                for (var i = 0; i < group.children.length; ++i) {
                    var track = group.children[i];
                    var ds = track.toDallianceSource();
                    if (!ds)
                        continue;

                    var r = makeElement('tr');
                    var bd = makeElement('td');
                    bd.style.textAlign = 'center';
                    
                    var b = makeElement('input');
                    b.type = 'checkbox';
                    b.dalliance_track = track;
                    if (__mapping) {
                        b.dalliance_mapping = __mapping;
                    }
                    b.checked = thisB.currentlyActive(ds); // FIXME!
                    bd.appendChild(b);
                    addButtons.push(b);
                    b.addEventListener('change', function(ev) {
                        if (ev.target.checked) {
                            thisB.addTier(ev.target.dalliance_track.toDallianceSource());
                        } else {
                            thisB.removeTier(ev.target.dalliance_track.toDallianceSource());
                        }
                    });

                    r.appendChild(bd);
                    var ld = makeElement('td');
                    ld.appendChild(document.createTextNode(track.shortLabel));
                    if (track.longLabel && track.longLabel.length > 0) {
                        thisB.makeTooltip(ld, track.longLabel);
                    }
                    r.appendChild(ld);
                    stabBody.appendChild(r);
                    ++idx;
                }
                ttab.appendChild(makeTreeTableSection(group.shortLabel, stab, gi==0));
                
            }
        }
        
        stabHolder.appendChild(ttab);
    };
    

    regButton.addEventListener('click', function(ev) {
        ev.preventDefault(); ev.stopPropagation();
        activateButton(addModeButtons, regButton);
        makeStab(thisB.availableSources);
    }, false);
    defButton.addEventListener('click', function(ev) {
        ev.preventDefault(); ev.stopPropagation();
        activateButton(addModeButtons, defButton);
        makeStab(new Observed(thisB.defaultSources));
    }, false);
    binButton.addEventListener('click', function(ev) {
        ev.preventDefault(); ev.stopPropagation();
        activateButton(addModeButtons, binButton);
        switchToBinMode();
    }, false);
    addHubButton.addEventListener('click', function(ev) {
        ev.preventDefault(); ev.stopPropagation();
        activateButton(addModeButtons, addHubButton);
        switchToHubConnectMode();
    }, false);


    function switchToBinMode() {
        customMode = 'bin';
        refreshButton.style.visibility = 'hidden';

        removeChildren(stabHolder);

        if (thisB.supportsBinary) {
            var pageHolder = makeElement('div', null, {}, {paddingLeft: '10px', paddingRight: '10px'});
            pageHolder.appendChild(makeElement('h3', 'Add custom URL-based data'));
            pageHolder.appendChild(makeElement('p', ['You can add indexed binary data hosted on an web server that supports CORS (', makeElement('a', 'full details', {href: 'http://www.biodalliance.org/bin.html'}), ').  Currently supported formats are bigwig, bigbed, and indexed BAM.']));

            pageHolder.appendChild(makeElement('br'));
            pageHolder.appendChild(document.createTextNode('URL: '));
            custURL = makeElement('input', '', {size: 80, value: 'http://www.biodalliance.org/datasets/ensGene.bb'}, {width: '100%'});
            pageHolder.appendChild(custURL);
            
            pageHolder.appendChild(makeElement('br'));
            pageHolder.appendChild(makeElement('b', '- or -'));
            pageHolder.appendChild(makeElement('br'));
            pageHolder.appendChild(document.createTextNode('File: '));
            custFile = makeElement('input', null, {type: 'file'});
            pageHolder.appendChild(custFile);
            
            pageHolder.appendChild(makeElement('p', 'Clicking the "Add" button below will initiate a series of test queries.'));

            stabHolder.appendChild(pageHolder);
            custURL.focus();
        } else {
            stabHolder.appendChild(makeElement('h2', 'Your browser does not support binary data'));
            stabHolder.appendChild(makeElement('p', 'Browsers currently known to support this feature include Google Chrome 9 or later and Mozilla Firefox 4 or later.'));
        }
        
    }

    function switchToHubConnectMode() {
        customMode = 'hub-connect';
        refreshButton.style.visibility = 'hidden';

        removeChildren(stabHolder);

        var pageHolder = makeElement('div', null, {}, {paddingLeft: '10px', paddingRight: '10px'});
        pageHolder.appendChild(makeElement('h3', 'Connect to a track hub.'));
        pageHolder.appendChild(makeElement('p', ['Enter the top-level URL (usually points to a file called "hub.txt" of a UCSC-style track hub']));
        
        custURL = makeElement('input', '', {size: 120, value: 'http://www.biodalliance.org/datasets/testhub/hub.txt'}, {width: '100%'});
        pageHolder.appendChild(custURL);
        
        stabHolder.appendChild(pageHolder);
        
        custURL.focus();
    }

    custButton.addEventListener('click', function(ev) {
        ev.preventDefault(); ev.stopPropagation();
        activateButton(addModeButtons, custButton);
        switchToCustomMode();
    }, false);

    function switchToCustomMode() {
        customMode = 'das';
        refreshButton.style.visibility = 'hidden';

        removeChildren(stabHolder);

        var customForm = makeElement('div', null, {},  {paddingLeft: '10px', paddingRight: '10px'});
        customForm.appendChild(makeElement('h3', 'Add custom DAS data'));
        customForm.appendChild(makeElement('p', 'This interface is intended for adding custom or lab-specific data.  Public data can be added more easily via the registry interface.'));
                
        customForm.appendChild(document.createTextNode('URL: '));
        customForm.appendChild(makeElement('br'));
        custURL = makeElement('input', '', {size: 80, value: 'http://www.derkholm.net:8080/das/medipseq_reads/'}, {width: '100%'});
        customForm.appendChild(custURL);

        customForm.appendChild(makeElement('p', 'Clicking the "Add" button below will initiate a series of test queries.  If the source is password-protected, you may be prompted to enter credentials.'));
        stabHolder.appendChild(customForm);

        custURL.focus();
    }



    var addButton = makeElement('button', 'Add', {className: 'btn btn-primary'});
    addButton.addEventListener('click', function(ev) {
        ev.stopPropagation(); ev.preventDefault();
        doAdd();
    }, false);

    function doAdd() {
        if (customMode) {
            if (customMode === 'das') {
                var curi = custURL.value.trim();
                if (!/^.+:\/\//.exec(curi)) {
                    curi = 'http://' + curi;
                }
                var nds = new DASSource({name: 'temporary', uri: curi});
                tryAddDAS(nds);
            } else if (customMode === 'bin') {
                var opts = {name: 'temporary'};
                var fileList = custFile.files;
                if (fileList && fileList.length > 0 && fileList[0]) {
                    opts.bwgBlob = fileList[0];
                    opts.noPersist = true;
                } else {
                    var curi = custURL.value.trim();
                    if (!/^.+:\/\//.exec(curi)) {
                        curi = 'http://' + curi;
                    }
                    opts.bwgURI = curi;
                }
                var nds = new DASSource(opts);
                tryAddBin(nds);
            } else if (customMode === 'reset') {
                switchToCustomMode();
            } else if (customMode === 'reset-bin') {
                switchToBinMode(); 
            } else if (customMode === 'reset-hub') {
                switchToHubConnectMode();
            } else if (customMode === 'prompt-bai') {
                var fileList = custFile.files;
                if (fileList && fileList.length > 0 && fileList[0]) {
                    dataToFinalize.baiBlob = fileList[0];
                    completeBAM(dataToFinalize);
                } else {
                    promptForBAI(dataToFinalize);
                }
            } else if (customMode === 'finalize') {
                dataToFinalize.name = custName.value;
                var m = custCS.value;
                if (m != '__default__') {
                    dataToFinalize.mapping = m;
                } else {
                    dataToFinalize.mapping = undefined;
                }
                if (custQuant) {
                    dataToFinalize.maxbins = custQuant.checked;
                }

                if (custUser.value.length > 1 && custPass.value.length > 1) {
                    dlog('password');
                    dataToFinalize.xUser = custUser.value;
                    dataToFinalize.xPass = custPass.value;
                }

                thisB.addTier(dataToFinalize);
                thisB.removeAllPopups();
            } else if (customMode === 'hub-connect') {
                var curi = custURL.value.trim();
                if (!/^.+:\/\//.exec(curi)) {
                    curi = 'http://' + curi;
                }
                console.log('hub: ' + curi);
                connectTrackHub(curi, function(hub, err) {
                    if (err) {
                        removeChildren(stabHolder);
                        stabHolder.appendChild(makeElement('h2', 'Error connecting to track hub'))
                        stabHolder.appendChild(makeElement('p', err));
                        customMode = 'reset-hub';
                        return;
                    } else {
                        if (thisB.coordSystem.ucscName && hub.genomes[thisB.coordSystem.ucscName]) {
                            thisB.hubs.push(curi);
                            thisB.hubObjects.push(hub);
                            
                            var hubButton = makeHubButton(hub);
                            modeButtonHolder.appendChild(hubButton);
                            activateButton(addModeButtons, hubButton);
                            
                        
                            // FIXME redundant with hub-tab click handler.
                        
                            hub.genomes[thisB.coordSystem.ucscName].getTracks(function(tracks, err) {
                                makeHubStab(tracks);
                            });
                        } else {
                            removeChildren(stabHolder);
                            stabHolder.appendChild(makeElement('h2', 'No data for this genome'))
                            stabHolder.appendChild(makeElement('p', 'This URL appears to be a valid track-hub, but it doesn\'t contain any data for the coordinate system of this browser'));
                            stabHolder.appendChild(makeElement('p', 'coordSystem.ucscName = ' + thisB.coordSystem.ucscName));
                            customMode = 'reset-hub';
                            return;
                        }
                    }
                });
            }
        } else {
            // No longer needed because of instant addition....

            /*
            for (var bi = 0; bi < addButtons.length; ++bi) {
                var b = addButtons[bi];
                if (b.checked) {
                    var nds = b.dalliance_source;
                    thisB.addTier(nds);
                }
            }
            */
            thisB.removeAllPopups();
        }
    };

    var tryAddDAS = function(nds, retry) {
        var knownSpace = thisB.knownSpace;
        if (!knownSpace) {
            alert("Can't confirm track-addition to an uninit browser.");
            return;
        }
        var tsm = Math.max(knownSpace.min, (knownSpace.min + knownSpace.max - 100) / 2)|0;
        var testSegment = new DASSegment(knownSpace.chr, tsm, Math.min(tsm + 99, knownSpace.max));
//        dlog('test segment: ' + testSegment);
        nds.features(testSegment, {}, function(features, status) {
            // dlog('status=' + status);
            if (status) {
                if (!retry) {
                    dlog('retrying with credentials');
                    nds.credentials = true;
                    tryAddDAS(nds, true);
                } else {
                    removeChildren(stabHolder);
                    stabHolder.appendChild(makeElement('h2', 'Custom data not found'));
                    stabHolder.appendChild(makeElement('p', 'DAS uri: ' + nds.uri + ' is not answering features requests'));
                    customMode = 'reset';
                    return;
                }
            } else {
                var nameExtractPattern = new RegExp('/([^/]+)/?$');
                var match = nameExtractPattern.exec(nds.uri);
                if (match) {
                    nds.name = match[1];
                }

                tryAddDASxSources(nds);
                return;
            }
        });
    }

    function tryAddDASxSources(nds, retry) {
        var uri = nds.uri;
        if (retry) {
            var match = /(.+)\/[^\/]+\/?/.exec(uri);
            if (match) {
                uri = match[1] + '/sources';
            }
        }
//        dlog('sourceQuery: ' + uri);
        function sqfail() {
            if (!retry) {
                return tryAddDASxSources(nds, true);
            } else {
                return addDasCompletionPage(nds);
            }
        }
        new DASRegistry(uri, {credentials: nds.credentials}).sources(
            function(sources) {
                if (!sources || sources.length == 0) {
                    return sqfail();
                } 
//                dlog('got ' + sources.length + ' sources');

                var fs = null;
                if (sources.length == 1) {
                    fs = sources[0];
                } else {
                    for (var i = 0; i < sources.length; ++i) {
                        if (sources[i].uri === nds.uri) {
//                            dlog('got match!');
                            fs = sources[i];
                            break;
                        }
                    }
                }

                var coordsDetermined = false, quantDetermined = false;
                if (fs) {
                    nds.name = fs.name;
                    nds.desc = fs.desc;
                    if (fs.maxbins) {
                        nds.maxbins = true;
                    } else {
                        nds.maxbins = false;
                    }
                    if (fs.capabilities) {
                        nds.capabilities = fs.capabilities;
                    }
                    quantDetermined = true
                    
                    if (fs.coords && fs.coords.length == 1) {
                        var coords = fs.coords[0];
                        if (coordsMatch(coords, thisB.coordSystem)) {
                            coordsDetermined = true;
                        } else if (thisB.chains) {
                            for (var k in thisB.chains) {
                                if (coordsMatch(coords, thisB.chains[k].coords)) {
                                    nds.mapping = k;
                                    coordsDetermined = true;
                                }
                            }
                        }
                    }
                    
                }
                return addDasCompletionPage(nds, coordsDetermined, quantDetermined);
            },
            function() {
                return sqfail();
            }
        );
    }

    var tryAddBin = function(nds) {
        var fetchable;
        if (nds.bwgURI) {
            fetchable = new URLFetchable(nds.bwgURI);
        } else {
            fetchable = new BlobFetchable(nds.bwgBlob);
        }

        fetchable.slice(0, 1<<16).fetch(function(result, error) {
            if (!result) {
                removeChildren(stabHolder);
                stabHolder.appendChild(makeElement('h2', 'Custom data not found'));
                if (nds.bwgURI) {
                    stabHolder.appendChild(makeElement('p', 'Data URI: ' + nds.bwgURI + ' is not accessible.'));
                } else {
                    stabHolder.appendChild(makeElement('p', 'File access failed, are you using an up-to-date browser?'));
                }

                if (error) {
                    stabHolder.appendChild(makeElement('p', '' + error));
                }
                stabHolder.appendChild(makeElement('p', 'If in doubt, please check that the server where the file is hosted supports CORS.'));
                customMode = 'reset-bin';
                return;
            }

            var ba = new Uint8Array(result);
            var magic = readInt(ba, 0);
            if (magic == BIG_WIG_MAGIC || magic == BIG_BED_MAGIC) {
                var nameExtractPattern = new RegExp('/?([^/]+?)(.bw|.bb|.bigWig|.bigBed)?$');
                var match = nameExtractPattern.exec(nds.bwgURI || nds.bwgBlob.name);
                if (match) {
                    nds.name = match[1];
                }

                return addDasCompletionPage(nds, false, false, true);
            } else {
                if (ba[0] != 31 || ba[1] != 139) {
                    return binFormatErrorPage();
                }
                var unc = unbgzf(result);
                var uncba = new Uint8Array(unc);
                magic = readInt(uncba, 0);
                if (magic == BAM_MAGIC) {
                    if (nds.bwgBlob) {
                        return promptForBAI(nds);
                    } else {
                        return completeBAM(nds);
                    }
                } else {
                    // maybe Tabix?
                   return binFormatErrorPage();
                }
            }
        });
    }

    function promptForBAI(nds) {
        removeChildren(stabHolder);
        customMode = 'prompt-bai'
        stabHolder.appendChild(makeElement('h2', 'Select an index file'));
        stabHolder.appendChild(makeElement('p', 'Dalliance requires a BAM index (.bai) file when displaying BAM data.  These normally accompany BAM files.  For security reasons, web applications like Dalliance can only access local files which you have explicity selected.  Please use the file chooser below to select the appropriate BAI file'));

        stabHolder.appendChild(document.createTextNode('Index file: '));
        custFile = makeElement('input', null, {type: 'file'});
        stabHolder.appendChild(custFile);
        dataToFinalize = nds;
    }

    function completeBAM(nds) {
        var indexF;
        if (nds.baiBlob) {
            indexF = new BlobFetchable(nds.baiBlob);
        } else {
            indexF = new URLFetchable(nds.bwgURI + '.bai');
        }
        indexF.slice(0, 256).fetch(function(r) {
                var hasBAI = false;
                if (r) {
                    var ba = new Uint8Array(r);
                    var magic2 = readInt(ba, 0);
                    hasBAI = (magic2 == BAI_MAGIC);
                }
                if (hasBAI) {
                    var nameExtractPattern = new RegExp('/?([^/]+?)(.bam)?$');
                    var match = nameExtractPattern.exec(nds.bwgURI || nds.bwgBlob.name);
                    if (match) {
                        nds.name = match[1];
                    }

                    nds.bamURI = nds.bwgURI;
                    nds.bamBlob = nds.bwgBlob;
                    nds.bwgURI = undefined;
                    nds.bwgBlob = undefined;
                            
                    return addDasCompletionPage(nds, false, false, true);
                } else {
                    return binFormatErrorPage('You have selected a valid BAM file, but a corresponding index (.bai) file was not found.  Please index your BAM (samtools index) and place the BAI file in the same directory');
                }
        });
    }

    function binFormatErrorPage(message) {
        removeChildren(stabHolder);
        message = message || 'Custom data format not recognized';
        stabHolder.appendChild(makeElement('h2', 'Error adding custom data'));
        stabHolder.appendChild(makeElement('p', message));
        stabHolder.appendChild(makeElement('p', 'Currently supported formats are bigBed, bigWig, and BAM.'));
        customMode = 'reset-bin';
        return;
    }
                     
    var addDasCompletionPage = function(nds, coordsDetermined, quantDetermined, quantIrrelevant) {
        removeChildren(stabHolder);
        stabHolder.appendChild(makeElement('h2', 'Add custom data: step 2'));
        stabHolder.appendChild(document.createTextNode('Label: '));
        custName = makeElement('input', '', {value: nds.name});
        stabHolder.appendChild(custName);


        // stabHolder.appendChild(document.createTextNode('User: '));
        custUser = makeElement('input', '');
        // stabHolder.appendChild(custUser);
        //stabHolder.appendChild(document.createTextNode('Pass: '));
        custPass = makeElement('input', '');
        // stabHolder.appendChild(custPass);
        

        stabHolder.appendChild(makeElement('br'));
        stabHolder.appendChild(makeElement('br'));
        stabHolder.appendChild(makeElement('h4', 'Coordinate system: '));
        custCS = makeElement('select', null);
        custCS.appendChild(makeElement('option', thisB.coordSystem.auth + thisB.coordSystem.version, {value: '__default__'}));
        if (thisB.chains) {
            for (var csk in thisB.chains) {
                var cs = thisB.chains[csk].coords;
                custCS.appendChild(makeElement('option', cs.auth + cs.version, {value: csk}));
            }
        }
        custCS.value = nds.mapping || '__default__';
        stabHolder.appendChild(custCS);

        if (coordsDetermined) {
            stabHolder.appendChild(makeElement('p', "(Based on server response, probably doesn't need changing.)"));
        } else {
            stabHolder.appendChild(makeElement('p', [makeElement('b', 'Warning: '), "unable to determine the correct value from server responses.  Please check carefully."]));
            stabHolder.appendChild(makeElement('p', "If you don't see the mapping you're looking for, please contact thomas@biodalliance.org"));
        }

        if (!quantIrrelevant) {
            stabHolder.appendChild(document.createTextNode('Quantitative: '));
            custQuant = makeElement('input', null, {type: 'checkbox', checked: true});
            if (typeof nds.maxbins !== 'undefined') {
                custQuant.checked = nds.maxbins;
            }
            stabHolder.appendChild(custQuant);
            if (quantDetermined) {
                stabHolder.appendChild(makeElement('p', "(Based on server response, probably doesn't need changing.)"));
            } else {
                stabHolder.appendChild(makeElement('p', [makeElement('b', "Warning: "), "unable to determine correct value.  If in doubt, leave checked."]));
            }
        }

        if (nds.bwgBlob) {
            stabHolder.appendChild(makeElement('p', [makeElement('b', 'Warning: '), 'data added from local file.  Due to the browser security model, the track will disappear if you reload Dalliance.']));
        }

        custName.focus();
        customMode = 'finalize';
        dataToFinalize = nds;
    }


    var canButton = makeElement('button', 'Cancel', {className: 'btn'});
    canButton.addEventListener('click', function(ev) {
        ev.stopPropagation(); ev.preventDefault();
        thisB.removeAllPopups();
    }, false);

    var refreshButton = makeElement('button', 'Refresh', {className: 'btn'});
    refreshButton.addEventListener('click', function(ev) {
        ev.stopPropagation(); ev.preventDefault();
        thisB.queryRegistry(__mapping);
    }, false);
    this.makeTooltip(refreshButton, 'Click to re-fetch data from the DAS registry');

    var buttonHolder = makeElement('div', [addButton, ' ', canButton, ' ', refreshButton]);
    buttonHolder.style.margin = '10px';
    asform.appendChild(buttonHolder);

    popup.appendChild(asform);
    makeStab(thisB.availableSources);

    return this.popit(ev, 'Add data sources...', popup, {width: 500});
}
/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2010
//
// twoBit.js: packed-binary reference sequences
//

var TWOBIT_MAGIC = 0x1a412743;

function TwoBitFile() {
}

function makeTwoBit(fetchable, cnt) {
    var tb = new TwoBitFile();
    tb.data = fetchable;

    tb.data.slice(0, 1024).fetch(function(r) {
        if (!r) {
            return cnt(null, "Couldn't access data");
        }
        var ba = new Uint8Array(r);
        var magic = readInt(ba, 0);
        if (magic != TWOBIT_MAGIC) {
            return cnt(null, "Not a .2bit fie");
        }

        var version = readInt(ba, 4);
        if (version != 0) {
            return cnt(null, 'Unsupported version ' + version);
        }
        
        tb.seqCount = readInt(ba, 8);
        tb.seqDict = {};
        var p = 16;
        for (var i = 0; i < tb.seqCount; ++i) {
            var ns = ba[p++];
            var name = '';
            for (var j = 1; j <= ns; ++j) {
                name += String.fromCharCode(ba[p++]);
            }
            var offset = readInt(ba, p);
            p += 4;
            tb.seqDict[name] = new TwoBitSeq(tb, offset);
        }
        return cnt(tb);
    });
}

TwoBitFile.prototype.getSeq = function(chr) {
    var seq = this.seqDict[chr];
    if (!seq) {
        seq = this.seqDict['chr' + chr];
    }
    return seq;
}

TwoBitFile.prototype.fetch = function(chr, min, max, cnt) {
    var seq = this.getSeq(chr);
    if (!seq) {
        return cnt(null, "Couldn't find " + chr);
    } else {
        seq.fetch(min, max, cnt);
    }
}

function TwoBitSeq(tbf, offset) {
    this.tbf = tbf;
    this.offset = offset;
}

TwoBitSeq.prototype.init = function(cnt) {
    if (this.seqOffset) {
        return cnt();
    }

    var thisB = this;
    thisB.tbf.data.slice(thisB.offset, 8).fetch(function(r1) {
        if (!r1) {
            return cnt('Fetch failed');
        }
        var ba = new Uint8Array(r1);
        thisB._length = readInt(ba, 0);
        thisB.nBlockCnt = readInt(ba, 4);
        thisB.tbf.data.slice(thisB.offset + 8, thisB.nBlockCnt*8 + 4).fetch(function(r2) {
            if (!r2) {
                return cnt('Fetch failed');
            }
            var ba = new Uint8Array(r2);
            var nbs = null;
            for (var b = 0; b < thisB.nBlockCnt; ++b) {
                var nbMin = readInt(ba, b * 4);
                var nbLen = readInt(ba, (b + thisB.nBlockCnt) * 4);
                var nb = new Range(nbMin, nbMin + nbLen - 1);
                if (!nbs) {
                    nbs = nb;
                } else {
                    nbs = union(nbs, nb);
                }
            }
            thisB.nBlocks = nbs;
            thisB.mBlockCnt = readInt(ba, thisB.nBlockCnt*8);
            thisB.seqLength = ((thisB._length + 3)/4)|0;
            thisB.seqOffset = thisB.offset + 16 + ((thisB.nBlockCnt + thisB.mBlockCnt) * 8);
            return cnt();
        });
    });
}

var TWOBIT_TABLE = ['T', 'C', 'A', 'G'];

TwoBitSeq.prototype.fetch = function(min, max, cnt) {
    --min; --max;       // Switch to zero-based.
    var thisB = this;
    this.init(function(error) {
        if (error) {
            return cnt(null, error);
        }

        var fetchMin = min >> 2;
        var fetchMax = max + 3 >> 2;
        if (fetchMin < 0 || fetchMax > thisB.seqLength) {
            return cnt('Coordinates out of bounds: ' + min + ':' + max);
        }

        thisB.tbf.data.slice(thisB.seqOffset + fetchMin, fetchMax - fetchMin).fetch(function(r) {
            if (r == null) {
                return cnt('SeqFetch failed');
            }
            var seqData = new Uint8Array(r);

            var nSpans = [];
            if (thisB.nBlocks) {
                var intr = intersection(new Range(min, max), thisB.nBlocks);
                if (intr) {
                    nSpans = intr.ranges();
                }
            }
            
            var seqstr = '';
            var ptr = min;
            function fillSeq(fsm) {
                while (ptr <= fsm) {
                    var bb = (ptr >> 2) - fetchMin;
                    var ni = ptr & 0x3;
                    var bv = seqData[bb];
                    var n;
                    if (ni == 0) {
                        n = (bv >> 6) & 0x3;
                    } else if (ni == 1) {
                        n = (bv >> 4) & 0x3;
                    } else if (ni == 2) {
                        n = (bv >> 2) & 0x3;
                    } else {
                        n = (bv) & 0x3;
                    }
                    seqstr += TWOBIT_TABLE[n];
                    ++ptr;
                }
            }
            
            for (var b = 0; b < nSpans.length; ++b) {
                var nb = nSpans[b];
                if (ptr > nb.min()) {
                    throw 'N mismatch...';
                }
                if (ptr < nb.min()) {
                    fillSeq(nb.min() - 1);
                }
                while (ptr < nb.max()) {
                    seqstr += 'N';
                    ++ptr;
                }
            }
            if (ptr < max) {
                fillSeq(max);
            }

            return cnt(seqstr);
        });
    });
}

TwoBitSeq.prototype.length = function(cnt) {
    var thisB = this;
    this.init(function(error) {
        if (error) {
            return cnt(null, error);
        } else {
            return cnt(thisB._length);
        }
    });
}
/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2010
//
// utils.js: odds, sods, and ends.
//

var NUM_REGEXP = new RegExp('[0-9]+');

function stringToNumbersArray(str) {
    var nums = new Array();
    var m;
    while (m = NUM_REGEXP.exec(str)) {
        nums.push(m[0]);
        str=str.substring(m.index + (m[0].length));
    }
    return nums;
}

var STRICT_NUM_REGEXP = new RegExp('^[0-9]+$');

function stringToInt(str) {
    str = str.replace(new RegExp(',', 'g'), '');
    if (!STRICT_NUM_REGEXP.test(str)) {
        return null;
    }
    return str|0;
}

function pushnew(a, v) {
    for (var i = 0; i < a.length; ++i) {
        if (a[i] == v) {
            return;
        }
    }
    a.push(v);
}

function pusho(obj, k, v) {
    if (obj[k]) {
        obj[k].push(v);
    } else {
        obj[k] = [v];
    }
}

function pushnewo(obj, k, v) {
    var a = obj[k];
    if (a) {
        for (var i = 0; i < a.length; ++i) {    // indexOf requires JS16 :-(.
            if (a[i] == v) {
                return;
            }
        }
        a.push(v);
    } else {
        obj[k] = [v];
    }
}


function pick(a, b, c, d)
{
    if (a) {
        return a;
    } else if (b) {
        return b;
    } else if (c) {
        return c;
    } else if (d) {
        return d;
    }
}

function pushnew(l, o)
{
    for (var i = 0; i < l.length; ++i) {
        if (l[i] == o) {
            return;
        }
    }
    l.push(o);
}

function maybeConcat(a, b) {
    var l = [];
    if (a) {
        for (var i = 0; i < a.length; ++i) {
            pushnew(l, a[i]);
        }
    }
    if (b) {
        for (var i = 0; i < b.length; ++i) {
            pushnew(l, b[i]);
        }
    }
    return l;
}

function arrayIndexOf(a, x) {
    if (!a) {
        return -1;
    }

    for (var i = 0; i < a.length; ++i) {
        if (a[i] === x) {
            return i;
        }
    }
    return -1;
}

function arrayRemove(a, x) {
    var i = arrayIndexOf(a, x);
    if (i >= 0) {
        a.splice(i, 1);
        return true;
    }
    return false;
}

//
// DOM utilities
//


function makeElement(tag, children, attribs, styles)
{
    var ele = document.createElement(tag);
    if (children) {
        if (! (children instanceof Array)) {
            children = [children];
        }
        for (var i = 0; i < children.length; ++i) {
            var c = children[i];
            if (typeof c == 'string') {
                c = document.createTextNode(c);
            }
            ele.appendChild(c);
        }
    }
    
    if (attribs) {
        for (var l in attribs) {
            ele[l] = attribs[l];
        }
    }
    if (styles) {
        for (var l in styles) {
            ele.style[l] = styles[l];
        }
    }
    return ele;
}

function makeElementNS(namespace, tag, children, attribs)
{
    var ele = document.createElementNS(namespace, tag);
    if (children) {
        if (! (children instanceof Array)) {
            children = [children];
        }
        for (var i = 0; i < children.length; ++i) {
            var c = children[i];
            if (typeof c == 'string') {
                c = document.createTextNode(c);
            }
            ele.appendChild(c);
        }
    }
    
    setAttrs(ele, attribs);
    return ele;
}

var attr_name_cache = {};

function setAttr(node, key, value)
{
    var attr = attr_name_cache[key];
    if (!attr) {
        var _attr = '';
        for (var c = 0; c < key.length; ++c) {
            var cc = key.substring(c, c+1);
            var lcc = cc.toLowerCase();
            if (lcc != cc) {
                _attr = _attr + '-' + lcc;
            } else {
                _attr = _attr + cc;
            }
        }
        attr_name_cache[key] = _attr;
        attr = _attr;
    }
    node.setAttribute(attr, value);
}

function setAttrs(node, attribs)
{
    if (attribs) {
        for (var l in attribs) {
            setAttr(node, l, attribs[l]);
        }
    }
}



function removeChildren(node)
{
    if (!node || !node.childNodes) {
        return;
    }

    while (node.childNodes.length > 0) {
        node.removeChild(node.firstChild);
    }
}



//
// WARNING: not for general use!
//

function miniJSONify(o, exc) {
    if (typeof o === 'undefined') {
        return 'undefined';
    } else if (o == null) {
        return 'null';
    } else if (typeof o == 'string') {
        return "'" + o + "'";
    } else if (typeof o == 'number') {
        return "" + o;
    } else if (typeof o == 'boolean') {
        return "" + o;
    } else if (typeof o == 'object') {
        if (o instanceof Array) {
            var s = null;
            for (var i = 0; i < o.length; ++i) {
                s = (s == null ? '' : (s + ', ')) + miniJSONify(o[i], exc);
            }
            return '[' + (s?s:'') + ']';
        } else {
            exc = exc || {};
            var s = null;
            for (var k in o) {
                if (exc[k])
                    continue;
                if (k != undefined && typeof(o[k]) != 'function') {
                    s = (s == null ? '' : (s + ', ')) + k + ': ' + miniJSONify(o[k], exc);
                }
            }
            return '{' + (s?s:'') + '}';
        }
    } else {
        return (typeof o);
    }
}

function shallowCopy(o) {
    n = {};
    for (k in o) {
        n[k] = o[k];
    }
    return n;
}

function Observed(x) {
    this.value = x;
    this.listeners = [];
}

Observed.prototype.addListener = function(f) {
    this.listeners.push(f);
}

Observed.prototype.addListenerAndFire = function(f) {
    this.listeners.push(f);
    f(this.value);
}

Observed.prototype.removeListener = function(f) {
    arrayRemove(this.listeners, f);
}

Observed.prototype.get = function() {
    return this.value;
}

Observed.prototype.set = function(x) {
    this.value = x;
    for (var i = 0; i < this.listeners.length; ++i) {
        this.listeners[i](x);
    }
}

function Awaited() {
    this.queue = [];
}

Awaited.prototype.provide = function(x) {
    if (this.res !== undefined) {
        throw "Resource has already been provided.";
    }

    this.res = x;
    for (var i = 0; i < this.queue.length; ++i) {
        this.queue[i](x);
    }
}

Awaited.prototype.await = function(f) {
    if (this.res !== undefined) {
        f(this.res);
        return this.res;
    } else {
        this.queue.push(f);
    }
}

function textXHR(url, callback) {
    var req = new XMLHttpRequest();
    req.onreadystatechange = function() {
	if (req.readyState == 4) {
	    if (req.status >= 300) {
		callback(null, 'Error code ' + req.status);
	    } else {
		callback(req.responseText);
	    }
	}
    };
    
    req.open('GET', url, true);
    req.responseType = 'text';
    req.send('');
}

function relativeURL(base, rel) {
    // FIXME quite naive -- good enough for trackhubs?

    var li = base.lastIndexOf('/');
    if (li >= 0) {
        return base.substr(0, li + 1) + rel;
    } else {
        return rel;
    }
}


//
// Missing APIs
// 

if (!('trim' in String.prototype)) {
    String.prototype.trim = function() {
        return this.replace(/^\s+/, '').replace(/\s+$/, '');
    };
}
/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2010
//
// version.js
//

var VERSION = {
    CONFIG: 3,
    MAJOR:  0,
    MINOR:  9,
    MICRO:  1,
    PATCH:  '',
    BRANCH: 'dev'
}

VERSION.toString = function() {
    var vs = '' + this.MAJOR + '.' + this.MINOR + '.' + this.MICRO;
    if (this.PATCH) {
        vs = vs + this.PATCH;
    }
    if (this.BRANCH && this.BRANCH != '') {
        vs = vs + '-' + this.BRANCH;
    }
    return vs;
}
/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2013
//
// browser-us.js: standard UI wiring
//

var molgenisUrl = location.hostname;
        if(location.port != ""){
        	molgenisUrl = molgenisUrl+":"+location.port;
        }

function formatLongInt(n) {
    return (n|0).toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

function parseLocCardinal(n, m) {
    var i = n.replace(/,/g, '');
    if (m === 'k' || m === 'K') {
        return i * 1000;
    } else if (m == 'm' || m === 'M') {
        return i * 1000000;
    } else {
        return i;
    }
}

/*
 * Quite a bit of this ought to be done using a templating system, but
 * since web-components isn't quite ready for prime time yet we'll stick
 * with constructing it all in Javascript for now...
 */

Browser.prototype.initUI = function(holder, genomePanel) {
    // FIXME shouldn't be hard-coded...
    document.head.appendChild(makeElement('link', '', {rel: 'stylesheet', href: this.uiPrefix + 'css/bootstrap-scoped.css'}));
    document.head.appendChild(makeElement('link', '', {rel: 'stylesheet', href: this.uiPrefix + 'css/dalliance-scoped.css'}));

    var b = this;
    var REGION_PATTERN = /([\d+,\w,\.,\_,\-]+):([0-9,]+)([KkMmGg])?([\-,\,.]+([0-9,]+)([KkMmGg])?)?/;
    // var REGION_PATTERN = /([\d+,\w,\.,\_,\-]+):([0-9,]+)([\-,\,.]+([0-9,]+))?/;

    if (!b.disableDefaultFeaturePopup) {
        this.addFeatureListener(function(ev, feature, hit, tier) {
            b.featurePopup(ev, feature, hit, tier);
            
            //BEGIN custom MOLGENIS code
            console.log('BEGIN custom MOLGENIS code'+hit[0].id);
                  if(hit[0].typeId == "mutation"){ // could also use hit.type?
                    var url = 'http://'+molgenisUrl+'/plugin/genomebrowser/data/'+ hit[0].id
                    console.log(url);
                    $.ajax({
                      url: url,
                      type: "GET",
                      dataType: "json",
                      success: function(data) {
                        console.log("Data returned : " + data);
                        
                        if (typeof data == 'object') {
                        	patientMutationTable(data);
                        }
                      },
                      error: function(jqXHR, textStatus, errorThrown) {
                        console.log("jqXHR : "+jqXHR + " text status : " + textStatus + " error : " + errorThrown);
                      }
                    });
                  }
            //END custom MOLGENIS code
        });
    }

    holder.classList.add('dalliance');
    var toolbar = makeElement('div', null, {className: 'btn-toolbar'});

    var title = b.coordSystem.speciesName + ' ' + b.coordSystem.auth + b.coordSystem.version;
    if (this.setDocumentTitle) {
        document.title = title + ' :: dalliance';
    }
    
    if (!this.noTitle) {
        toolbar.appendChild(makeElement('div', makeElement('h4', title, {}, {margin: '0px'}), {className: 'btn-group'}, {verticalAlign: 'top'}));
    }

    var locField = makeElement('input', '', {className: 'loc-field'});
    b.makeTooltip(locField, 'Enter a genomic location or gene name');
    var locStatusField = makeElement('p', '', {className: 'loc-status'});
    toolbar.appendChild(makeElement('div', [locField, locStatusField], {className: 'btn-group'}, {verticalAlign: 'top', marginLeft: '10px', marginRight: '5px'}));

    var zoomInBtn = makeElement('a', [makeElement('i', null, {className: 'icon-zoom-in'})], {className: 'btn'});
    var zoomSlider = makeElement('input', '', {type: 'range', min: 100, max: 250}, {width: '200px'});  // NB min and max get overwritten.
    var zoomOutBtn = makeElement('a', [makeElement('i', null, {className: 'icon-zoom-out'})], {className: 'btn'});
    toolbar.appendChild(makeElement('div', [zoomInBtn,
                                            makeElement('span', zoomSlider, {className: 'btn'}),
                                            zoomOutBtn], {className: 'btn-group'}, {verticalAlign: 'top'}));

    var addTrackBtn = makeElement('a', [makeElement('i', null, {className: 'icon-plus'})], {className: 'btn'});
    var favBtn = makeElement('a', [makeElement('i', null, {className: 'icon-bookmark'})], {className: 'btn'});
    var svgBtn = makeElement('a', [makeElement('i', null, {className: 'icon-print'})], {className: 'btn'});
    var resetBtn = makeElement('a', [makeElement('i', null, {className: 'icon-refresh'})], {className: 'btn'});
    var optsButton = makeElement('div', [makeElement('i', null, {className: 'icon-cog'})], {className: 'btn'});

    var helpButton = makeElement('div', [makeElement('i', null, {className: 'icon-info-sign'})], {className: 'btn'});
    
    toolbar.appendChild(makeElement('div', [addTrackBtn,
                                            // favBtn,
                                            svgBtn,
                                            resetBtn,
                                            optsButton], {className: 'btn-group'}, {verticalAlign: 'top'}));

    toolbar.appendChild(makeElement('div', [helpButton], {className: 'btn-group'}, {verticalAlign: 'top'}));

    holder.appendChild(toolbar);
    holder.appendChild(genomePanel);

    this.addViewListener(function(chr, min, max, _oldZoom, zoom) {
        locField.value = '';
        locField.placeholder = ('chr' + chr + ':' + formatLongInt(min) + '..' + formatLongInt(max));
        zoomSlider.min = zoom.min;
        zoomSlider.max = zoom.max;
        zoomSlider.value = zoom.current;
        if (b.storeStatus) {
            b.storeStatus();
        }
    });

    this.addTierListener(function() {
        if (b.storeStatus) {
            b.storeStatus();
        }
    });

    locField.addEventListener('keydown', function(ev) {
        if (ev.keyCode == 40) {
            ev.preventDefault(); ev.stopPropagation();
            b.setSelectedTier(0);
        } if (ev.keyCode == 10 || ev.keyCode == 13) {
            ev.preventDefault();

            var g = locField.value;
            var m = REGION_PATTERN.exec(g);
            // console.log(m);

            var setLocationCB = function(err) {
                    if (err) {
                        locStatusField.innerText = '' + err;
                    } else {
                        locStatusField.innerText = '';
                    }
                };

            if (m) {
                var chr = m[1], start, end;
                if (m[5]) {
                    start = parseLocCardinal(m[2],  m[3]);
                    end = parseLocCardinal(m[5], m[6]);
                } else {
                    var width = b.viewEnd - b.viewStart + 1;
                    start = (parseLocCardinal(m[2], m[3]) - (width/2))|0;
                    end = start + width - 1;
                }
                b.setLocation(chr, start, end, setLocationCB);
            } else {
                if (!g || g.length == 0) {
                    return false;
                }

                b.searchEndpoint.features(null, {group: g, type: 'transcript'}, function(found) {        // HAXX
                    if (!found) found = [];
                    var min = 500000000, max = -100000000;
                    var nchr = null;
                    for (var fi = 0; fi < found.length; ++fi) {
                        var f = found[fi];
                        
                        if (f.label.toLowerCase() != g.toLowerCase()) {
                            // ...because Dazzle can return spurious overlapping features.
                            continue;
                        }

                        if (nchr == null) {
                            nchr = f.segment;
                        }
                        min = Math.min(min, f.min);
                        max = Math.max(max, f.max);
                    }

                    if (!nchr) {
                        locStatusField.innerText = "no match for '" + g + "' (search should improve soon!)";
                    } else {
                        b.highlightRegion(nchr, min, max);
                    
                        var padding = Math.max(2500, (0.3 * (max - min + 1))|0);
                        b.setLocation(nchr, min - padding, max + padding, setLocationCB);
                    }
                }, false);
            }

        }
    }, false); 


  this.addRegionSelectListener(function(chr, min, max) {
      // console.log('chr' + chr + ':' + min + '..' + max);
      // b.highlightRegion(chr, min, max);
      // console.log('selected ' + b.featuresInRegion(chr, min, max).length);
  });

  this.addTierListener(function() {
      if (b.storeStatus) {
          b.storeStatus();
      }
  });


    
    var trackAddPopup;
    addTrackBtn.addEventListener('click', function(ev) {
        if (trackAddPopup && trackAddPopup.displayed) {
            b.removeAllPopups();
        } else {
            trackAddPopup = b.showTrackAdder(ev);
        }
    }, false);
    b.makeTooltip(addTrackBtn, 'Add a new track from the registry or an indexed file.');

    zoomInBtn.addEventListener('click', function(ev) {
      ev.stopPropagation(); ev.preventDefault();

      b.zoomStep(-10);
    }, false);
    b.makeTooltip(zoomInBtn, 'Zoom in');

    zoomOutBtn.addEventListener('click', function(ev) {
      ev.stopPropagation(); ev.preventDefault();

      b.zoomStep(10);
    }, false);
    b.makeTooltip(zoomOutBtn, 'Zoom out');

    zoomSlider.addEventListener('change', function(ev) {
	b.zoomSliderValue = (1.0 * zoomSlider.value);
	b.zoom(Math.exp((1.0 * zoomSlider.value) / b.zoomExpt));
    }, false);
    zoomSlider.min = b.zoomMin;
    zoomSlider.max = b.zoomMax;

    favBtn.addEventListener('click', function(ev) {
       ev.stopPropagation(); ev.preventDefault();
    }, false);
    b.makeTooltip(favBtn, 'Favourite regions');

    svgBtn.addEventListener('click', function(ev) {
       ev.stopPropagation(); ev.preventDefault();
        b.saveSVG();
    }, false);
    b.makeTooltip(svgBtn, 'Export publication-quality SVG.');

    resetBtn.addEventListener('click', function(ev) {
       ev.stopPropagation(); ev.preventDefault();

       for (var i = b.tiers.length - 1; i >= 0; --i) {
           b.removeTier({index: i});
       }
       for (var i = 0; i < b.defaultSources.length; ++i) {
           b.addTier(b.defaultSources[i]);
       }

        b.setLocation(b.defaultChr, b.defaultStart, b.defaultEnd);
       
            //BEGIN custom MOLGENIS code
            var url = 'http://'+molgenisUrl+'/plugin/genomebrowser/data/';
            console.log(url);
            $.ajax({
              url: url,
              type: "GET",
              dataType: "json",
              success: function(data) {
                console.log("Data returned : " + data);
                
                if (typeof data == 'object') {
                	patientMutationTable(data);
                }
                },
                error: function(jqXHR, textStatus, errorThrown) {
                  console.log("jqXHR : "+jqXHR + " text status : " + textStatus + " error : " + errorThrown);
                }
             });
            //END custom MOLGENIS code
        
    }, false);
    b.makeTooltip(resetBtn, 'Reset to default tracks and view.');

    var optsPopup;
    optsButton.addEventListener('click', function(ev) {
        ev.stopPropagation(); ev.preventDefault();

        b.toggleOptsPopup(ev);
    }, false);
    b.makeTooltip(optsButton, 'Configure options.');

    helpButton.addEventListener('click', function(ev) {
        ev.stopPropagation(); ev.preventDefault();
        b.toggleHelpPopup(ev);
    });
    b.makeTooltip(helpButton, 'Help; Keyboard shortcuts.');

    b.addTierSelectionWrapListener(function(dir) {
        if (dir < 0) {
            b.setSelectedTier(null);
            locField.focus();
        }
    });
    //BEGIN custom MOLGENIS code
    var url = 'http://'+molgenisUrl+'/plugin/genomebrowser/data/';
    console.log(url);
    $.ajax({
      url: url,
      type: "GET",
      dataType: "json",
      success: function(data) {
        console.log("Data returned : " + data);
        
        if (typeof data == 'object') {
        	patientMutationTable(data);
        }
        },
        error: function(jqXHR, textStatus, errorThrown) {
          console.log("jqXHR : "+jqXHR + " text status : " + textStatus + " error : " + errorThrown);
        }
     });
    //END custom MOLGENIS code
  }

Browser.prototype.toggleHelpPopup = function(ev) {
    if (this.helpPopup && this.helpPopup.displayed) {
        this.removeAllPopups();
    } else {
    	// BEGIN custom MOLGENIS code
    	var helpFrame = makeElement('iframe', null, {src: this.uiPrefix + 'css/index.html'}, {width: '490px', height: '500px'});
    	// END custom MOLGENIS code
    	this.helpPopup = this.popit(ev, 'Help', helpFrame, {width: 500});
    }
}

Browser.prototype.toggleOptsPopup = function(ev) {
    var b = this;

    if (this.optsPopup && this.optsPopup.displayed) {
        this.removeAllPopups();
    } else {
        var optsForm = makeElement('form', null, {className: 'popover-content form-horizontal'});
        var optsTable = makeElement('table');
        optsTable.cellPadding = 5;
        var scrollModeButton = makeElement('input', '', {type: 'checkbox', checked: b.reverseScrolling});
        scrollModeButton.addEventListener('change', function(ev) {
            b.reverseScrolling = scrollModeButton.checked;
        }, false);
        optsTable.appendChild(makeElement('tr', [makeElement('td', 'Reverse trackpad scrolling', {align: 'right'}), makeElement('td', scrollModeButton)]));


        var rulerSelect = makeElement('select');
        rulerSelect.appendChild(makeElement('option', 'Left', {value: 'left'}));
        rulerSelect.appendChild(makeElement('option', 'Center', {value: 'center'}));
        rulerSelect.appendChild(makeElement('option', 'Right', {value: 'right'}));
        rulerSelect.appendChild(makeElement('option', 'None', {value: 'none'}));
        rulerSelect.value = b.rulerLocation;
        rulerSelect.addEventListener('change', function(ev) {
            b.rulerLocation = rulerSelect.value;
            b.positionRuler();
            for (var ti = 0; ti < b.tiers.length; ++ti) {
                b.tiers[ti].paintQuant();
            }
        }, false);
        optsTable.appendChild(makeElement('tr', [makeElement('td', 'Vertical guideline', {align: 'right'}), makeElement('td', rulerSelect)]));
        
        optsForm.appendChild(optsTable);
        this.removeAllPopups();
        this.optsPopup = this.popit(ev, 'Options', optsForm, {width: 500});
    }
}

// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2010
//
// feature-draw.js: new feature-tier renderer
//

function BoxGlyph(x, y, width, height, fill, stroke, alpha, radius) {
    this.x = x;
    this.y = y;
    this._width = width;
    this._height = height;
    this.fill = fill;
    this.stroke = stroke;
    this._alpha = alpha;
    this._radius = radius || 0;
}

BoxGlyph.prototype.draw = function(g) {
    var r = this._radius;

    g.beginPath();

    if (r > 0) {
	g.moveTo(this.x + r, this.y);
	g.lineTo(this.x + this._width - r, this.y);
	g.arcTo(this.x + this._width, this.y, this.x + this._width, this.y + r, r);
	g.lineTo(this.x + this._width, this.y + this._height - r);
	g.arcTo(this.x + this._width, this.y + this._height, this.x + this._width - r, this.y + this._height, r);
	g.lineTo(this.x + r, this.y + this._height);
	g.arcTo(this.x, this.y + this._height, this.x, this.y + this._height - r, r);
	g.lineTo(this.x, this.y + r);
	g.arcTo(this.x, this.y, this.x + r, this.y, r);
    } else {
	g.lineJoin = 'miter';
	g.lineCap = 'square';
	g.moveTo(this.x, this.y);
	g.lineTo(this.x + this._width, this.y);
	g.lineTo(this.x + this._width, this.y + this._height);
	g.lineTo(this.x, this.y + this._height);
	g.lineTo(this.x, this.y);
    }

    g.closePath();

    if (this._alpha != null) {
	g.save();
	g.globalAlpha = this._alpha;
    }
    
    if (this.fill) {
	g.fillStyle = this.fill;
	g.fill();
    }
    if (this.stroke) {
	g.strokeStyle = this.stroke;
	g.lineWidth = 0.5;
	g.stroke();
    }

    if (this._alpha != null) {
	g.restore();
    }
}

BoxGlyph.prototype.toSVG = function() {
    var s = makeElementNS(NS_SVG, 'rect', null,
			 {x: this.x, 
			  y: this.y, 
			  width: this._width, 
			  height: this._height,
			  stroke: this.stroke || 'none',
			  strokeWidth: 0.5,
			  fill: this.fill || 'none'});
    if (this._alpha != null) {
	s.setAttribute('opacity', this._alpha);
    }

    return s;
}

BoxGlyph.prototype.min = function() {
    return this.x;
}

BoxGlyph.prototype.max = function() {
    return this.x + this._width;
}

BoxGlyph.prototype.height = function() {
    return this.y + this._height;
}


function GroupGlyph(glyphs, connector) {
    this.glyphs = glyphs;
    this.connector = connector;
    this.h = glyphs[0].height();

    var cov = new Range(glyphs[0].min(), glyphs[0].max());
    for (g = 1; g < glyphs.length; ++g) {
	var gg = glyphs[g];
	cov = union(cov, new Range(gg.min(), gg.max()));
	this.h = Math.max(this.h, gg.height());
    }
    this.coverage = cov;
}

GroupGlyph.prototype.draw = function(g) {
    for (var i = 0; i < this.glyphs.length; ++i) {
	var gl = this.glyphs[i];
	gl.draw(g);
    }

    var ranges = this.coverage.ranges();
    for (var r = 1; r < ranges.length; ++r) {
	var gl = ranges[r];
	var last = ranges[r - 1];
	if (last && gl.min() > last.max()) {
	    var start = last.max();
	    var end = gl.min();
	    var mid = (start+end)/2
	    
	    g.beginPath();
	    if (this.connector === 'hat+') {
		g.moveTo(start, this.h/2);
		g.lineTo(mid, 0);
		g.lineTo(end, this.h/2);
	    } else if (this.connector === 'hat-') {
		g.moveTo(start, this.h/2);
		g.lineTo(mid, this.h);
		g.lineTo(end, this.h/2);
	    } else if (this.connector === 'collapsed+') {
		g.moveTo(start, this.h/2);
		g.lineTo(end, this.h/2);
		if (end - start > 4) {
		    g.moveTo(mid - 2, (this.h/2) - 5);
		    g.lineTo(mid + 2, this.h/2);
		    g.lineTo(mid - 2, (this.h/2) + 5);
		}
	    } else if (this.connector === 'collapsed-') {
		g.moveTo(start, this.h/2);
		g.lineTo(end, this.h/2);
		if (end - start > 4) {
		    g.moveTo(mid + 2, (this.h/2) - 5);
		    g.lineTo(mid - 2, this.h/2);
		    g.lineTo(mid + 2, (this.h/2) + 5);
		}
	    } else {
		g.moveTo(start, this.h/2);
		g.lineTo(end, this.h/2);
	    }
	    g.stroke();
	}
	last = gl;
    }
}

function SVGPath() {
    this.ops = [];
}

SVGPath.prototype.moveTo = function(x, y) {
    this.ops.push('M ' + x + ' ' + y);
}

SVGPath.prototype.lineTo = function(x, y) {
    this.ops.push('L ' + x + ' ' + y);
}

SVGPath.prototype.closePath = function() {
    this.ops.push('Z');
}

SVGPath.prototype.toPathData = function() {
    return this.ops.join(' ');
}

GroupGlyph.prototype.toSVG = function() {
    var g = makeElementNS(NS_SVG, 'g');
    for (var i = 0; i < this.glyphs.length; ++i) {
	g.appendChild(this.glyphs[i].toSVG());
    }

    var ranges = this.coverage.ranges();
    for (var r = 1; r < ranges.length; ++r) {
	var gl = ranges[r];
	var last = ranges[r - 1];
	if (last && gl.min() > last.max()) {
	    var start = last.max();
	    var end = gl.min();
	    var mid = (start+end)/2

	    var p = new SVGPath();

	    if (this.connector === 'hat+') {
		p.moveTo(start, this.h/2);
		p.lineTo(mid, 0);
		p.lineTo(end, this.h/2);
	    } else if (this.connector === 'hat-') {
		p.moveTo(start, this.h/2);
		p.lineTo(mid, this.h);
		p.lineTo(end, this.h/2);
	    } else if (this.connector === 'collapsed+') {
		p.moveTo(start, this.h/2);
		p.lineTo(end, this.h/2);
		if (end - start > 4) {
		    p.moveTo(mid - 2, (this.h/2) - 5);
		    p.lineTo(mid + 2, this.h/2);
		    p.lineTo(mid - 2, (this.h/2) + 5);
		}
	    } else if (this.connector === 'collapsed-') {
		p.moveTo(start, this.h/2);
		p.lineTo(end, this.h/2);
		if (end - start > 4) {
		    p.moveTo(mid + 2, (this.h/2) - 5);
		    p.lineTo(mid - 2, this.h/2);
		    p.lineTo(mid + 2, (this.h/2) + 5);
		}
	    } else {
		p.moveTo(start, this.h/2);
		p.lineTo(end, this.h/2);
	    }

	    var path = makeElementNS(
		NS_SVG, 'path',
		null,
		{d: p.toPathData(),
		 fill: 'none',
		 stroke: 'black',
		 strokeWidth: 0.5});
	    g.appendChild(path);
	}
    }

    return g;

    
}

GroupGlyph.prototype.min = function() {
    return this.coverage.min();
}

GroupGlyph.prototype.max = function() {
    return this.coverage.max();
}

GroupGlyph.prototype.height = function() {
    return this.h;
}


function LineGraphGlyph(points, color, height) {
    this.points = points;
    this.color = color;
    this._height = height || 50;
}

LineGraphGlyph.prototype.min = function() {
    return this.points[0];
};

LineGraphGlyph.prototype.max = function() {
    return this.points[this.points.length - 2];
};

LineGraphGlyph.prototype.height = function() {
    return this._height;
}

LineGraphGlyph.prototype.draw = function(g) {
    g.save();
    g.strokeStyle = this.color;
    g.lineWidth = 2;
    g.beginPath();
    for (var i = 0; i < this.points.length; i += 2) {
	var x = this.points[i];
	var y = this.points[i + 1];
	if (i == 0) {
	    g.moveTo(x, y);
	} else {
	    g.lineTo(x, y);
	}
    }
    g.stroke();
    g.restore();
}

LineGraphGlyph.prototype.toSVG = function() {
    var p = new SVGPath();
    for (var i = 0; i < this.points.length; i += 2) {
	var x = this.points[i];
	var y = this.points[i + 1];
	if (i == 0) {
	    p.moveTo(x, y);
	} else {
	    p.lineTo(x, y);
	}
    }
    
    return makeElementNS(
	NS_SVG, 'path',
	null,
	{d: p.toPathData(),
	 fill: 'none',
	 stroke: this.color,
	 strokeWidth: '2px'});
}

function LabelledGlyph(glyph, text) {
    this.glyph = glyph;
    this.text = text;
    this.textLen = GLOBAL_GC.measureText(text).width + 10;
    this.bump = glyph.bump;
}

LabelledGlyph.prototype.toSVG = function() {
    return makeElementNS(NS_SVG, 'g',
        [this.glyph.toSVG(),
         makeElementNS(NS_SVG, 'text', this.text, {x: this.glyph.min(), y: this.glyph.height() + 15})]);
}

LabelledGlyph.prototype.min = function() {
    return this.glyph.min();
}

LabelledGlyph.prototype.max = function() {
    return Math.max(this.glyph.max(), (1.0*this.glyph.min()) + this.textLen);
}

LabelledGlyph.prototype.height = function() {
    return this.glyph.height() + 20;
}

LabelledGlyph.prototype.draw = function(g) {
    this.glyph.draw(g);
    g.fillStyle = 'black';
    g.fillText(this.text, this.glyph.min(), this.glyph.height() + 15);
}



function CrossGlyph(x, height, stroke) {
    this._x = x;
    this._height = height;
    this._stroke = stroke;
}

CrossGlyph.prototype.draw = function(g) {
    var hh = this._height/2;
    
    g.beginPath();
    g.moveTo(this._x, 0);
    g.lineTo(this._x, this._height);
    g.moveTo(this._x - hh, hh);
    g.lineTo(this._x + hh, hh);

    g.strokeStyle = this._stroke;
    g.lineWidth = 1;

    g.stroke();
}

CrossGlyph.prototype.toSVG = function() {
    var hh = this._height/2;

    var g = new SVGPath();
    g.moveTo(this._x, 0);
    g.lineTo(this._x, this._height);
    g.moveTo(this._x - hh, hh);
    g.lineTo(this._x + hh, hh);
    
    return makeElementNS(
	NS_SVG, 'path',
	null,
	{d: g.toPathData(),
	 fill: 'none',
	 stroke: this._stroke,
	 strokeWidth: '1px'});
}

CrossGlyph.prototype.min = function() {
    return this._x - this._height/2;
}

CrossGlyph.prototype.max = function() {
    return this._x + this._height/2;
}

CrossGlyph.prototype.height = function() {
    return this._height;
}

function ExGlyph(x, height, stroke) {
    this._x = x;
    this._height = height;
    this._stroke = stroke;
}

ExGlyph.prototype.draw = function(g) {
    var hh = this._height/2;
    
    g.beginPath();
    g.moveTo(this._x - hh, 0);
    g.lineTo(this._x + hh, this._height);
    g.moveTo(this._x - hh, this._height);
    g.lineTo(this._x + hh, 0);

    g.strokeStyle = this._stroke;
    g.lineWidth = 1;

    g.stroke();
}

ExGlyph.prototype.toSVG = function() {
    var hh = this._height/2;

    var g = new SVGPath();
    g.moveTo(this._x - hh, 0);
    g.lineTo(this._x + hh, this._height);
    g.moveTo(this._x - hh, this._height);
    g.lineTo(this._x + hh, 0);
    
    return makeElementNS(
	NS_SVG, 'path',
	null,
	{d: g.toPathData(),
	 fill: 'none',
	 stroke: this._stroke,
	 strokeWidth: '1px'});
}

ExGlyph.prototype.min = function() {
    return this._x - this._height/2;
}

ExGlyph.prototype.max = function() {
    return this._x + this._height/2;
}

ExGlyph.prototype.height = function() {
    return this._height;
}

function TriangleGlyph(x, height, dir, width, stroke) {
    this._x = x;
    this._height = height;
    this._dir = dir;
    this._width = width;
    this._stroke = stroke;
}

TriangleGlyph.prototype.drawPath = function(g) {
    var hh = this._height/2;
    var hw = this._width/2;

    if (this._dir === 'S') {
	g.moveTo(this._x, this._height);
	g.lineTo(this._x - hw, 0);
	g.lineTo(this._x + hw, 0);
    } else if (this._dir === 'W') {
	g.moveTo(this._x + hw, hh);
	g.lineTo(this._x - hw, 0);
	g.lineTo(this._x - hw, this._height);
    } else if (this._dir === 'E') {
	g.moveTo(this._x - hw, hh);
	g.lineTo(this._x + hw, 0);
	g.lineTo(this._x + hw, this._height);
    } else {
	g.moveTo(this._x , 0);
	g.lineTo(this._x + hw, this._height);
	g.lineTo(this._x - hw, this._height);
    }

    g.closePath();
}

TriangleGlyph.prototype.draw = function(g) {
    g.beginPath();
    this.drawPath(g);
    g.fillStyle = this._stroke;
    g.fill();
}

TriangleGlyph.prototype.toSVG = function() {


    var g = new SVGPath();
    this.drawPath(g);
    
    return makeElementNS(
	NS_SVG, 'path',
	null,
	{d: g.toPathData(),
	 fill: this._stroke});
}

TriangleGlyph.prototype.min = function() {
    return this._x - this._height/2;
}

TriangleGlyph.prototype.max = function() {
    return this._x + this._height/2;
}

TriangleGlyph.prototype.height = function() {
    return this._height;
}




function DotGlyph(x, height, stroke) {
    this._x = x;
    this._height = height;
    this._stroke = stroke;
}

DotGlyph.prototype.draw = function(g) {
    var hh = this._height/2;
    g.fillStyle = this._stroke;
    g.beginPath();
    g.arc(this._x, hh, hh, 0, 6.29);
    g.fill();
}

DotGlyph.prototype.toSVG = function() {
    var hh = this._height/2;
    return makeElementNS(
	NS_SVG, 'circle',
	null,
	{cx: this._x, cy: hh, r: hh,
	 fill: this._stroke,
	 strokeWidth: '1px'});
}

DotGlyph.prototype.min = function() {
    return this._x - this._height/2;
}

DotGlyph.prototype.max = function() {
    return this._x + this._height/2;
}

DotGlyph.prototype.height = function() {
    return this._height;
}


function PaddedGlyph(glyph, minp, maxp) {
    this.glyph = glyph;
    this._min = minp;
    this._max = maxp;
    if (glyph) {
	this.bump = glyph.bump;
    }
}

PaddedGlyph.prototype.draw = function(g) {
    if (this.glyph) 
	this.glyph.draw(g);
}

PaddedGlyph.prototype.toSVG = function() {
    if (this.glyph) {
	return this.glyph.toSVG();
    } else {
	return makeElementNS(NS_SVG, 'g');
    }
}

PaddedGlyph.prototype.min = function() {
    return this._min;
}

PaddedGlyph.prototype.max = function() {
    return this._max;
}

PaddedGlyph.prototype.height = function() {
    if (this.glyph) {
	return this.glyph.height();
    } else {
	return 1;
    }
}


function AArrowGlyph(min, max, height, fill, stroke, ori) {
    this._min = min;
    this._max = max;
    this._height = height;
    this._fill = fill;
    this._stroke = stroke;
    this._ori = ori;
}

AArrowGlyph.prototype.min = function() {
    return this._min;
}

AArrowGlyph.prototype.max = function() {
    return this._max;
}

AArrowGlyph.prototype.height = function() {
    return this._height;
}

AArrowGlyph.prototype.makePath = function(g) {
    var maxPos = this._max;
    var minPos = this._min;
    var height = this._height;
    var lInset = 0;
    var rInset = 0;
    var minLength = this._height + 2;
    var instep = 0.333333 * this._height;
    var y = 0;

    if (this._ori) {
	if (this._ori === '+') {
	    rInset = 0.5 * this._height;
	} else if (this._ori === '-') {
	    lInset = 0.5 * this._height;
	}
    }

    if (maxPos - minPos < minLength) {
        minPos = (maxPos + minPos - minLength) / 2;
        maxPos = minPos + minLength;
    }

    g.moveTo(minPos + lInset, y+instep);
    g.lineTo(maxPos - rInset, y+instep);
    g.lineTo(maxPos - rInset, y);
    g.lineTo(maxPos, y + this._height/2);
    g.lineTo(maxPos - rInset, y+height);
    g.lineTo(maxPos - rInset, y+instep+instep);
    g.lineTo(minPos + lInset, y+instep+instep);
    g.lineTo(minPos + lInset, y+height);
    g.lineTo(minPos, y+height/2);
    g.lineTo(minPos + lInset, y);
    g.lineTo(minPos + lInset, y+instep);
}

AArrowGlyph.prototype.draw = function(g) {
    g.beginPath();
    this.makePath(g);

    if (this._fill) {
	g.fillStyle = this._fill;
	g.fill();
    } 
    if (this._stroke) {
	g.strokeStyle = this._stroke;
	g.stroke();
    }
}

AArrowGlyph.prototype.toSVG = function() {
    var g = new SVGPath();
    this.makePath(g);
    
    return makeElementNS(
	NS_SVG, 'path',
	null,
	{d: g.toPathData(),
	 fill: this._fill || 'none',
	 stroke: this._stroke || 'none'});
}

function SpanGlyph(min, max, height, stroke) {
    this._min = min;
    this._max = max;
    this._height = height;
    this._stroke = stroke;
}

SpanGlyph.prototype.min = function() {return this._min};
SpanGlyph.prototype.max = function() {return this._max};
SpanGlyph.prototype.height = function() {return this._height};


SpanGlyph.prototype.drawPath = function(g) {
    var minPos = this._min, maxPos = this._max;
    var height = this._height, hh = height/2;
    g.moveTo(minPos, hh);
    g.lineTo(maxPos, hh);
    g.moveTo(minPos, 0);
    g.lineTo(minPos, height);
    g.moveTo(maxPos, 0);
    g.lineTo(maxPos, height);
}


SpanGlyph.prototype.draw = function(g) {
    g.beginPath();
    this.drawPath(g);
    g.strokeStyle = this._stroke;
    g.stroke();
}

SpanGlyph.prototype.toSVG = function() {
    var g = new SVGPath();
    this.drawPath(g);
    
    return makeElementNS(
	NS_SVG, 'path',
	null,
	{d: g.toPathData(),
	 stroke: this._stroke || 'none'});
}




function LineGlyph(min, max, height, style, strand, stroke) {
    this._min = min;
    this._max = max;
    this._height = height;
    this._style = style;
    this._strand = strand;
    this._stroke = stroke;
}

LineGlyph.prototype.min = function() {return this._min};
LineGlyph.prototype.max = function() {return this._max};
LineGlyph.prototype.height = function() {return this._height};

LineGlyph.prototype.drawPath = function(g) {
    var minPos = this._min, maxPos = this._max;
    var height = this._height, hh = height/2;

    if (this._style === 'hat') {
	g.moveTo(minPos, hh);
	g.lineTo((minPos + maxPos)/2, this._strand === '-' ? height : 0);
	g.lineTo(maxPos, hh);
    } else {
	g.moveTo(minPos, hh);
	g.lineTo(maxPos, hh);
    }
}


LineGlyph.prototype.draw = function(g) {
    g.beginPath();
    this.drawPath(g);
    g.strokeStyle = this._stroke;
    if (this._style === 'dashed' && g.setLineDash) {
	g.save();
	g.setLineDash([3]);
	g.stroke();
	g.restore();
    } else {
	g.stroke();
    }
}

LineGlyph.prototype.toSVG = function() {
    var g = new SVGPath();
    this.drawPath(g);
    
    var opts = {d: g.toPathData(),
	    stroke: this._stroke || 'none'};
    if (this._style === 'dashed') {
	opts['strokeDasharray'] = '3';
    }

    return makeElementNS(
	NS_SVG, 'path',
	null, opts
    );
}





function PrimersGlyph(min, max, height, fill, stroke) {
    this._min = min;
    this._max = max;
    this._height = height;
    this._fill = fill;
    this._stroke = stroke;
}

PrimersGlyph.prototype.min = function() {return this._min};
PrimersGlyph.prototype.max = function() {return this._max};
PrimersGlyph.prototype.height = function() {return this._height};


PrimersGlyph.prototype.drawStemPath = function(g) {
    var minPos = this._min, maxPos = this._max;
    var height = this._height, hh = height/2;
    g.moveTo(minPos, hh);
    g.lineTo(maxPos, hh);
}

PrimersGlyph.prototype.drawTrigsPath = function(g) {
    var minPos = this._min, maxPos = this._max;
    var height = this._height, hh = height/2;
    g.moveTo(minPos, 0);
    g.lineTo(minPos + height, hh);
    g.lineTo(minPos, height);
    g.lineTo(minPos, 0);
    g.moveTo(maxPos, 0);
    g.lineTo(maxPos - height, hh);
    g.lineTo(maxPos, height);
    g.lineTo(maxPos, 0);
}


PrimersGlyph.prototype.draw = function(g) {
    g.beginPath();
    this.drawStemPath(g);
    g.strokeStyle = this._stroke;
    g.stroke();
    g.beginPath();
    this.drawTrigsPath(g);
    g.fillStyle = this._fill;
    g.fill();
}

PrimersGlyph.prototype.toSVG = function() {
    var s = new SVGPath();
    this.drawStemPath(s);
    var t = new SVGPath();
    this.drawTrigsPath(t);
    
    return makeElementNS(
	NS_SVG, 'g',
	[makeElementNS(
	    NS_SVG, 'path',
	    null,
	    {d: s.toPathData(),
	     stroke: this._stroke || 'none'}),
	 makeElementNS(
	     NS_SVG, 'path',
	     null,
	     {d: t.toPathData(),
	      fill: this._fill || 'none'})]);
}

function ArrowGlyph(min, max, height, color, parallel, sw, ne) {
    this._min = min;
    this._max = max;
    this._height = height;
    this._color = color;
    this._parallel = parallel;
    this._sw = sw;
    this._ne = ne;
}

ArrowGlyph.prototype.min = function() {return this._min};
ArrowGlyph.prototype.max = function() {return this._max};
ArrowGlyph.prototype.height = function() {return this._height};

ArrowGlyph.prototype.drawPath = function(g) {
    var min = this._min, max = this._max, height = this._height;
    
    if (this._parallel) {
	var hh = height/2;
	var instep = 0.4 * height;
	if (this._sw) {
	    g.moveTo(min + hh, height-instep);
	    g.lineTo(min + hh, height);
	    g.lineTo(min, hh);
	    g.lineTo(min + hh, 0);
	    g.lineTo(min + hh, instep);
	} else {
	    g.moveTo(min, height-instep);
	    g.lineTo(min, instep);
	}
	if (this._ne) {
	    g.lineTo(max - hh, instep);
	    g.lineTo(max - hh, 0);
	    g.lineTo(max, hh);
	    g.lineTo(max - hh, height);
	    g.lineTo(max - hh, height - instep);
	} else {
	    g.lineTo(max, instep);
	    g.lineTo(max, height-instep);
	}
	g.closePath();
    } else {
	var mid = (min+max)/2;
	var instep = 0.4*(max-min);
	var th = height/3;

	if (this._ne) {
	    g.moveTo(min + instep, th);
	    g.lineTo(min, th);
	    g.lineTo(mid, 0);
	    g.lineTo(max, th);
	    g.lineTo(max - instep, th);
	} else {
	    g.moveTo(min+instep, 0);
	    g.lineTo(max-instep, 0);
	}
	if (this._sw) {
	    g.lineTo(max - instep, height-th);
	    g.lineTo(max, height-th);
	    g.lineTo(mid, height);
	    g.lineTo(min, height-th)
	    g.lineTo(min + instep, height-th);
	} else {
	    g.lineTo(max - instep, height);
	    g.lineTo(min + instep, height);
	}
	g.closePath();
    }
}

ArrowGlyph.prototype.draw = function(g) {
    g.beginPath();
    this.drawPath(g);
    g.fillStyle = this._color;
    g.fill();
}

ArrowGlyph.prototype.toSVG = function() {
    var g = new SVGPath();
    this.drawPath(g);
    
    return makeElementNS(
	NS_SVG, 'path',
	null,
	{d: g.toPathData(),
	 fill: this._color});
}


function TooManyGlyph(min, max, height, fill, stroke) {
    this._min = min;
    this._max = max;
    this._height = height;
    this._fill = fill;
    this._stroke = stroke;
}

TooManyGlyph.prototype.min = function() {return this._min};
TooManyGlyph.prototype.max = function() {return this._max};
TooManyGlyph.prototype.height = function() {return this._height};

TooManyGlyph.prototype.toSVG = function() {
    return makeElementNS(NS_SVG, 'rect', null,
			 {x: this._min, 
			  y: 0, 
			  width: this._max - this._min, 
			  height: this._height,
			  stroke: this._stroke || 'none',
			  fill: this._fill || 'none'});
}

TooManyGlyph.prototype.draw = function(g) {
    if (this._fill) {
	g.fillStyle = this._fill;
	g.fillRect(this._min, 0, this._max - this._min, this._height);
    }
    if (this._stroke) {
	g.strokeStyle = this._stroke;
	g.strokeRect(this._min, 0, this._max - this._min, this._height);
	g.beginPath();
	for (var n = 2; n < this._height; n += 3) {
	    g.moveTo(this._min, n);
	    g.lineTo(this._max, n);
	}
	g.stroke();
    }
}

function TextGlyph(min, max, height, fill, string) {
    this._min = min;
    this._max = max;
    this._height = height;
    this._fill = fill;
    this._string = string;
    this._textLen = GLOBAL_GC.measureText(string).width;
}

TextGlyph.prototype.min = function() {return this._min};
TextGlyph.prototype.max = function() {return Math.max(this._max, this._min + this._textLen)};
TextGlyph.prototype.height = function() {return this._height};

TextGlyph.prototype.draw = function(g) {
    g.fillStyle = this._fill;
    g.fillText(this._string, this._min, this._height - 4);
}

TextGlyph.prototype.toSVG = function() {
    return makeElementNS(NS_SVG, 'text', this._string, {x: this._min, y: this._height - 4});
}



function SequenceGlyph(min, max, height, seq, ref) {
    this._min = min;
    this._max = max;
    this._height = height;
    this._seq = seq;
    this._ref = ref;
}

SequenceGlyph.prototype.min = function() {return this._min};
SequenceGlyph.prototype.max = function() {return this._max};
SequenceGlyph.prototype.height = function() {return this._height};


SequenceGlyph.prototype.draw = function(gc) {
    var seq = this._seq;
    var scale = (this._max - this._min + 1) / this._seq.length;

    for (var p = 0; p < seq.length; ++p) {
	var base = seq.substr(p, 1).toUpperCase();
	var color = baseColors[base];
	if (!color) {
	    color = 'gray';
	}

	/*
	if (this._ref) {
	    var refbase = seq.substr(p, 1).toUpperCase();
	    if (refbase === base) {
		color = 'gray';
	    } else {
		color = 'red';
            }
        }*/

	gc.fillStyle = color;

	if (scale >= 8) {
	    gc.fillText(base, this._min + p*scale, 8);
	} else {
	    gc.fillRect(this._min + p*scale, 0, scale, this._height);
	}
    }
}

SequenceGlyph.prototype.toSVG = function() {
    var seq = this._seq;
    var scale = (this._max - this._min + 1) / this._seq.length;
    var  g = makeElementNS(NS_SVG, 'g'); 

    for (var p = 0; p < seq.length; ++p) {
	var base = seq.substr(p, 1).toUpperCase();
	var color = baseColors[base];
	if (!color) {
	    color = 'gray';
	}

	if (scale >= 8) {
	    g.appendChild(
		    makeElementNS(NS_SVG, 'text', base, {
			x: this._min + p*scale,
			y: 8,
			fill: color}));
	} else {
	    g.appendChild(
		    makeElementNS(NS_SVG, 'rect', null, {
			x:this._min + p*scale,
			y: 0,
			width: scale,
			height: this._height,
	                fill: color}));

	}
    }

    return g;
}


function TranslatedGlyph(glyph, x, y, height) {
    this.glyph = glyph;
    this._height = height;
    this._x = x;
    this._y = y;
}

TranslatedGlyph.prototype.height = function() {
    return this._height;
}

TranslatedGlyph.prototype.min = function() {
    return this.glyph.min() + this._x;
}

TranslatedGlyph.prototype.max = function() {
    return this.glyph.max() + this._x;
}

TranslatedGlyph.prototype.draw = function(g) {
    g.save();
    g.translate(this._x, this._y);
    this.glyph.draw(g);
    g.restore();
}

TranslatedGlyph.prototype.toSVG = function() {
    var s =  this.glyph.toSVG();
    s.setAttribute('transform', 'translate(' + this._x + ',' + this._y + ')');
    return s;
}

function PointGlyph(x, y, height, fill) {
    this._x = x;
    this._y = y;
    this._height = height;
    this._fill = fill;
}

PointGlyph.prototype.min = function() {
    return this._x - 2;
}

PointGlyph.prototype.max = function() {
    return this._x + 2;
}

PointGlyph.prototype.height = function() {
    return this._height;
}

PointGlyph.prototype.draw = function(g) {
    g.save();
    g.globalAlpha = 0.3;
    g.fillStyle = this._fill;
    g.beginPath();
    g.arc(this._x, this._y, 1.5, 0, 6.29);
    g.fill();
    g.restore();
}

PointGlyph.prototype.toSVG = function() {
    return makeElementNS(
	NS_SVG, 'circle',
	null,
	{cx: this._x, cy: this._y, r: 2,
	 fill: this._fill,
	 stroke: 'none'});
}


function GridGlyph(height) {
    this._height = height || 50;
}

GridGlyph.prototype.notSelectable = true;

GridGlyph.prototype.min = function() {
    return -100000;
};

GridGlyph.prototype.max = function() {
    return 100000;
};

GridGlyph.prototype.height = function() {
    return this._height;
}

GridGlyph.prototype.draw = function(g) {
    g.save();
    g.strokeStyle = 'black'
    g.lineWidth = 0.1;

    g.beginPath();
    for (var y = 0; y <= this._height; y += 10) {
	g.moveTo(-5000, y);
	g.lineTo(5000, y);
    }
    g.stroke();
    g.restore();
}

GridGlyph.prototype.toSVG = function() {
    var p = new SVGPath();
    for (var y = 0; y <= this._height; y += 10) {
	p.moveTo(-5000, y);
	p.lineTo(5000, y);
    }
    
    return makeElementNS(
	NS_SVG, 'path',
	null,
	{d: p.toPathData(),
	 fill: 'none',
	 stroke: 'black',
	 strokeWidth: '0.1px'});
}

function StarGlyph(x, r, points, fill, stroke) {
    this._x = x;
    this._r = r;
    this._points = points;
    this._fill = fill;
    this._stroke = stroke;
}

StarGlyph.prototype.min = function() {
    return this._x - this._r;
}

StarGlyph.prototype.max = function() {
    return this._x + this._r;
}

StarGlyph.prototype.height = function() {
    return 2 * this._r;
}

StarGlyph.prototype.drawPath = function(g) {
    var midX = this._x, midY = this._r, r = this._r;
    for (var p = 0; p < this._points; ++p) {
	var theta = (p * 6.28) / this._points;
	var px = midX + r*Math.sin(theta);
	var py = midY - r*Math.cos(theta);
	if (p == 0) {
	    g.moveTo(px, py);
	} else {
	    g.lineTo(px, py);
	}
	theta = ((p+0.5) * 6.28) / this._points;
	px = midX + 0.4*r*Math.sin(theta);
	py = midY - 0.4*r*Math.cos(theta);
	g.lineTo(px, py);
    }
    g.closePath();
}

StarGlyph.prototype.draw = function(g) {
    g.beginPath();
    this.drawPath(g);
    g.fillStyle = this._fill;
    g.fill();
}

StarGlyph.prototype.toSVG = function() {
    var g = new SVGPath();
    this.drawPath(g);
    
    return makeElementNS(
	NS_SVG, 'path',
	null,
	{d: g.toPathData(),
	 fill: this._fill});
}
/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2013
//
// session.js
//

Browser.prototype.nukeStatus = function() {
    delete localStorage['dalliance.' + this.cookieKey + '.view-chr'];
    delete localStorage['dalliance.' + this.cookieKey + '.view-start'];
    delete localStorage['dalliance.' + this.cookieKey + '.view-end'];
    delete localStorage['dalliance.' + this.cookieKey + '.sources'];
    delete localStorage['dalliance.' + this.cookieKey + '.version'];

    delete localStorage['dalliance.' + this.cookieKey + '.reverse-scrolling'];
    delete localStorage['dalliance.' + this.cookieKey + '.ruler-location'];
}

Browser.prototype.storeStatus = function() {
    if (!this.cookieKey || this.noPersist) {
        return;
    }

    localStorage['dalliance.' + this.cookieKey + '.view-chr'] = this.chr;
    localStorage['dalliance.' + this.cookieKey + '.view-start'] = this.viewStart|0;
    localStorage['dalliance.' + this.cookieKey + '.view-end'] = this.viewEnd|0
    if (this.currentSeqMax) {
	localStorage['dalliance.' + this.cookieKey + '.current-seq-length'] = this.currentSeqMax;
    }

    var currentSourceList = [];
    for (var t = 0; t < this.tiers.length; ++t) {
        var ts = this.tiers[t].dasSource;
        if (!ts.noPersist) {
            currentSourceList.push(this.tiers[t].dasSource);
        }
    }
    localStorage['dalliance.' + this.cookieKey + '.sources'] = JSON.stringify(currentSourceList);
    localStorage['dalliance.' + this.cookieKey + '.hubs'] = JSON.stringify(this.hubs);
    localStorage['dalliance.' + this.cookieKey + '.reverse-scrolling'] = this.reverseScrolling;
    localStorage['dalliance.' + this.cookieKey + '.ruler-location'] = this.rulerLocation;
    
    localStorage['dalliance.' + this.cookieKey + '.version'] = VERSION.CONFIG;
}

Browser.prototype.restoreStatus = function() {
    if (this.noPersist)
        return;
    
    var storedConfigVersion = localStorage['dalliance.' + this.cookieKey + '.version'];
    if (storedConfigVersion) {
        storedConfigVersion = storedConfigVersion|0;
    } else {
        storedConfigVersion = -100;
    }
    if (VERSION.CONFIG != storedConfigVersion) {
        return;
    }

    var storedConfigHash = localStorage['dalliance.' + this.cookieKey + '.configHash'] || '';
    var pageConfigHash = hex_sha1(miniJSONify(this.sources));
    if (pageConfigHash != storedConfigHash) {
        localStorage['dalliance.' + this.cookieKey + '.configHash'] = pageConfigHash;
        return;
    }

    var defaultSourcesByConfigHash = {};
    for (var si = 0; si < this.sources.length; ++si) {
        var source = this.sources[si];
        defaultSourcesByConfigHash[hex_sha1(miniJSONify(source))] = source;
    }

    var qChr = localStorage['dalliance.' + this.cookieKey + '.view-chr'];
    var qMin = localStorage['dalliance.' + this.cookieKey + '.view-start']|0;
    var qMax = localStorage['dalliance.' + this.cookieKey + '.view-end']|0;
    if (qChr && qMin && qMax) {
	this.chr = qChr;
	this.viewStart = qMin;
	this.viewEnd = qMax;
	
	var csm = localStorage['dalliance.' + this.cookieKey + '.current-seq-length'];
	if (csm) {
	    this.currentSeqMax = csm|0;
	}
    }
    var rs = localStorage['dalliance.' + this.cookieKey + '.reverse-scrolling'];
    this.reverseScrolling = (rs && rs == 'true');

    var rl = localStorage['dalliance.' + this.cookieKey + '.ruler-location'];
    if (rl)
        this.rulerLocation = rl;

    var sourceStr = localStorage['dalliance.' + this.cookieKey + '.sources'];
    if (sourceStr) {
	this.sources = JSON.parse(sourceStr);
        for (var si = 0; si < this.sources.length; ++si) {
            var source = this.sources[si];
            var hash = hex_sha1(miniJSONify(source, {props: true, coords: true}));
            var oldSource = defaultSourcesByConfigHash[hash];
            if (oldSource) {
                if (oldSource.featureInfoPlugin) {
                    // console.log('revivifying ' + hash);
                    source.featureInfoPlugin = oldSource.featureInfoPlugin;
                }
            }
        }
    }

    var hubStr = localStorage['dalliance.' + this.cookieKey + '.hubs'];
    if (hubStr) {
        this.hubs = JSON.parse(hubStr);
    }
}
/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2013
//
// sourceadapters.js
//

DasTier.prototype.initSources = function() {
    var thisTier = this;
    var fs = new DummyFeatureSource(), ss;

    if (this.dasSource.tier_type == 'sequence') {
        if (this.dasSource.twoBitURI) {
            ss = new TwoBitSequenceSource(this.dasSource);
        } else {
            ss = new DASSequenceSource(this.dasSource);
        }
    } else {
        fs = this.browser.createFeatureSource(this.dasSource);
    }

    this.featureSource = fs;
    this.sequenceSource = ss;
}

Browser.prototype.createFeatureSource = function(config) {
    var fs;

    if (config.bwgURI || config.bwgBlob) {
        fs =  new BWGFeatureSource(config);
    } else if (config.bamURI || config.bamBlob) {
        fs = new BAMFeatureSource(config);
    } else if (config.bamblrURI) {
        fs = new BamblrFeatureSource(config);
    } else if (config.jbURI) {
        fs = new JBrowseFeatureSource(config);
    } else if (config.tier_type == 'ensembl') {
        fs = new EnsemblFeatureSource(config);
    } else if (config.uri || config.features_uri) {
        fs = new DASFeatureSource(config);
    }

    if (config.overlay) {
        var sources = [];
        if (fs)
            sources.push(fs);

        for (var oi = 0; oi < config.overlay.length; ++oi) {
            sources.push(this.createFeatureSource(config.overlay[oi]));
        }
        fs = new OverlayFeatureSource(sources, config);
    }

    if (config.mapping) {
        fs = new MappedFeatureSource(fs, this.chains[config.mapping]);
    }

    if (config.name && !fs.name) {
        fs.name = config.name;
    }

    return fs;
}


function DASFeatureSource(dasSource) {
    this.dasSource = dasSource;
}

DASFeatureSource.prototype.getStyleSheet = function(callback) {
    this.dasSource.stylesheet(function(stylesheet) {
	callback(stylesheet);
    }, function() {
	callback(null, "Couldn't fetch DAS stylesheet");
    });
}

DASFeatureSource.prototype.fetch = function(chr, min, max, scale, types, pool, callback) {
    if (types && types.length == 0) {
        callback(null, [], scale);
        return;
    }

    if (!this.dasSource.uri) {
        // FIXME should this be making an error callback???
        return;
    }

    if (this.dasSource.dasStaticFeatures && this.cachedStaticFeatures) {
        return callback(null, this.cachedStaticFeatures, this.cachedStaticScale);
    }

    var tryMaxBins = (this.dasSource.maxbins !== false);
    var fops = {
        type: types
    };
    if (tryMaxBins) {
        fops.maxbins = 1 + (((max - min) / scale) | 0);
    }
    
    var thisB = this;
    this.dasSource.features(
        new DASSegment(chr, min, max),
        fops,
        function(features, status) {
            var retScale = scale;
            if (!tryMaxBins) {
                retScale = 0.1;
            }
            if (!status && thisB.dasSource.dasStaticFeatures) {
                thisB.cachedStaticFeatures = features;
                thisB.cachedStaticScale = retScale;
            }
            callback(status, features, retScale);
        }
    );
}

DASFeatureSource.prototype.findNextFeature = this.sourceFindNextFeature = function(chr, pos, dir, callback) {
    if (this.dasSource.capabilities && arrayIndexOf(this.dasSource.capabilities, 'das1:adjacent-feature') >= 0) {
        var thisB = this;
        if (this.dasAdjLock) {
            return dlog('Already looking for a next feature, be patient!');
        }
        this.dasAdjLock = true;
        var fops = {
            adjacent: chr + ':' + (pos|0) + ':' + (dir > 0 ? 'F' : 'B')
        }
        var types = thisTier.getDesiredTypes(thisTier.browser.scale);
        if (types) {
            fops.types = types;
        }
        thisTier.dasSource.features(null, fops, function(res) {
            thisB.dasAdjLock = false;
            if (res.length > 0 && res[0] != null) {
                dlog('DAS adjacent seems to be working...');
                callback(res[0]);
            }
        });
    }
};

function DASSequenceSource(dasSource) {
    this.dasSource = dasSource;
    this.awaitedEntryPoints = new Awaited();

    var thisB = this;
    this.dasSource.entryPoints(
        function(ep) {
            thisB.awaitedEntryPoints.provide(ep);
        });
}


DASSequenceSource.prototype.fetch = function(chr, min, max, pool, callback) {
    this.dasSource.sequence(
        new DASSegment(chr, min, max),
        function(seqs) {
            if (seqs.length == 1) {
                return callback(null, seqs[0]);
            } else {
                return callback("Didn't get sequence");
            }
        }
    );
}

DASSequenceSource.prototype.getSeqInfo = function(chr, cnt) {
    this.awaitedEntryPoints.await(function(ep) {
        for (var epi = 0; epi < ep.length; ++epi) {
            if (ep[epi].name == chr) {
                return cnt({length: ep[epi].end});
            }
        }
        return cnt();
    });
}
    

function TwoBitSequenceSource(source) {
    var thisB = this;
    this.source = source;
    this.twoBit = new Awaited();
    makeTwoBit(new URLFetchable(source.twoBitURI), function(tb, error) {
        if (error) {
            dlog(error);
        } else {
            thisB.twoBit.provide(tb);
        }
    });
}

TwoBitSequenceSource.prototype.fetch = function(chr, min, max, pool, callback) {
        this.twoBit.await(function(tb) {
            tb.fetch(chr, min, max,
                     function(seq, err) {
                         if (err) {
                             return callback(err, null);
                         } else {
                             var sequence = new DASSequence(chr, min, max, 'DNA', seq);
                             return callback(null, sequence);
                         }
                     })
        });
}

TwoBitSequenceSource.prototype.getSeqInfo = function(chr, cnt) {
    this.twoBit.await(function(tb) {
        var seq = tb.getSeq(chr);
        if (seq) {
            tb.getSeq(chr).length(function(l) {
                cnt({length: l});
            });
        } else {
            cnt();
        }
    });
}

DASFeatureSource.prototype.getScales = function() {
    return [];
}

var bwg_preflights = {};

function BWGFeatureSource(bwgSource) {
    var thisB = this;
    this.bwgSource = this.opts = bwgSource;    
    thisB.bwgHolder = new Awaited();

    if (this.opts.preflight) {
        var pfs = bwg_preflights[this.opts.preflight];
        if (!pfs) {
            pfs = new Awaited();
            bwg_preflights[this.opts.preflight] = pfs;

            var req = new XMLHttpRequest();
            req.onreadystatechange = function() {
                if (req.readyState == 4) {
                    if (req.status == 200) {
                        pfs.provide('success');
                    } else {
                        pfs.provide('failure');
                    }
                }
            };
            req.open('get', this.opts.preflight + '?' + hex_sha1('salt' + Date.now()), true);    // Instead, ensure we always preflight a unique URI.
            if (this.opts.credentials) {
                req.withCredentials = true;
            }
            req.send('');
        }
        pfs.await(function(status) {
            if (status === 'success') {
                thisB.init();
            }
        });
    } else {
        thisB.init();
    }
}

BWGFeatureSource.prototype.init = function() {
    var thisB = this;
    var make, arg;
    if (this.bwgSource.bwgURI) {
        make = makeBwgFromURL;
        arg = this.bwgSource.bwgURI;
    } else {
        make = makeBwgFromFile;
        arg = this.bwgSource.bwgBlob;
    }

    make(arg, function(bwg) {
        thisB.bwgHolder.provide(bwg);
    }, this.opts.credentials);
}

BWGFeatureSource.prototype.capabilities = function() {
    var caps = {leap: true};
    if (this.bwgHolder.res && this.bwgHolder.res.type == 'bigwig')
        caps.quantLeap = true;
    return caps;
}

BWGFeatureSource.prototype.fetch = function(chr, min, max, scale, types, pool, callback) {
    var thisB = this;
    this.bwgHolder.await(function(bwg) {
        if (bwg == null) {
            return callback("Can't access binary file", null, null);
        }

        // dlog('bwg: ' + bwg.name + '; want scale: ' + scale);
        var data;
        // dlog(miniJSONify(types));
        var wantDensity = !types || types.length == 0 || arrayIndexOf(types, 'density') >= 0;
/*        if (wantDensity) {
            dlog('want density; scale=' + scale);
        } */
        if (thisB.opts.clientBin) {
            wantDensity = false;
        }
        if (bwg.type == 'bigwig' || wantDensity || (typeof thisB.opts.forceReduction !== 'undefined')) {
            var zoom = -1;
            for (var z = 0; z < bwg.zoomLevels.length; ++z) {
                if (bwg.zoomLevels[z].reduction <= scale) {
                    zoom = z;
                } else {
                    break;
                }
            }
            if (typeof thisB.opts.forceReduction !== 'undefined') {
                zoom = thisB.opts.forceReduction;
            }
           // dlog('selected zoom: ' + zoom);
            if (zoom < 0) {
                data = bwg.getUnzoomedView();
            } else {
                data = bwg.getZoomedView(zoom);
            }
        } else {
            data = bwg.getUnzoomedView();
        }
        data.readWigData(chr, min, max, function(features) {
            var fs = 1000000000;
            if (bwg.type === 'bigwig') {
                var is = (max - min) / features.length / 2;
                if (is < fs) {
                    fs = is;
                }
            }
            if (thisB.opts.link) {
                for (var fi = 0; fi < features.length; ++fi) {
                    var f = features[fi];
                    if (f.label) {
                        f.links = [new DASLink('Link', thisB.opts.link.replace(/\$\$/, f.label))];
                    }
                }
            }
            callback(null, features, fs);
        });
    });
}

BWGFeatureSource.prototype.quantFindNextFeature = function(chr, pos, dir, threshold, callback) {
    var beforeQFNF = Date.now()|0;
    this.bwgHolder.res.thresholdSearch(chr, pos, dir, threshold, function(a, b) {
        var afterQFNF = Date.now()|0;
        console.log('QFNF took ' + (afterQFNF - beforeQFNF) + 'ms');
        return callback(a, b);
    });
}

BWGFeatureSource.prototype.findNextFeature = function(chr, pos, dir, callback) {
    this.bwgHolder.res.getUnzoomedView().getFirstAdjacent(chr, pos, dir, function(res) {
        if (res.length > 0 && res[0] != null) {
            callback(res[0]);
        }
    });
}

BWGFeatureSource.prototype.getScales = function() {
    var bwg = this.bwgHolder.res;
    if (bwg /* && bwg.type == 'bigwig' */) {
        var scales = [1];  // Can we be smarter about inferring baseline scale?
        for (var z = 0; z < bwg.zoomLevels.length; ++z) {
            scales.push(bwg.zoomLevels[z].reduction);
        }
        return scales;
    } else {
        return null;
    }
}

BWGFeatureSource.prototype.getStyleSheet = function(callback) {
    this.bwgHolder.await(function(bwg) {
        if (!bwg) {
            return callback(null, 'bbi error');
        }

	/* What to do about this...?
        if (thisTier.dasSource.collapseSuperGroups === undefined) {
            if (bwg.definedFieldCount == 12 && bwg.fieldCount >= 14) {
                thisTier.dasSource.collapseSuperGroups = true;
                thisTier.bumped = false;
            }
        }*/

	var stylesheet = new DASStylesheet();
        if (bwg.type == 'bigbed') {
            var wigStyle = new DASStyle();
            wigStyle.glyph = 'BOX';
            wigStyle.FGCOLOR = 'black';
            wigStyle.BGCOLOR = 'blue'
            wigStyle.HEIGHT = 8;
            wigStyle.BUMP = true;
            wigStyle.LABEL = true;
            wigStyle.ZINDEX = 20;
            stylesheet.pushStyle({type: 'bigwig'}, null, wigStyle);
	    
            wigStyle.glyph = 'BOX';
            wigStyle.FGCOLOR = 'black';
            wigStyle.BGCOLOR = 'red'
            wigStyle.HEIGHT = 10;
            wigStyle.BUMP = true;
            wigStyle.ZINDEX = 20;
            stylesheet.pushStyle({type: 'bb-translation'}, null, wigStyle);
                    
            var tsStyle = new DASStyle();
            tsStyle.glyph = 'BOX';
            tsStyle.FGCOLOR = 'black';
            tsStyle.BGCOLOR = 'white';
            tsStyle.HEIGHT = 10;
            tsStyle.ZINDEX = 10;
            tsStyle.BUMP = true;
            tsStyle.LABEL = true;
            stylesheet.pushStyle({type: 'bb-transcript'}, null, tsStyle);

            var densStyle = new DASStyle();
            densStyle.glyph = 'HISTOGRAM';
            densStyle.COLOR1 = 'white';
            densStyle.COLOR2 = 'black';
            densStyle.HEIGHT=30;
            stylesheet.pushStyle({type: 'density'}, null, densStyle);
        } else {
            var wigStyle = new DASStyle();
            wigStyle.glyph = 'HISTOGRAM';
            wigStyle.COLOR1 = 'white';
            wigStyle.COLOR2 = 'black';
            wigStyle.HEIGHT=30;
            stylesheet.pushStyle({type: 'default'}, null, wigStyle);
        }

	return callback(stylesheet);
    });
}

function BamblrFeatureSource(bamblrSource) {
    this.bamblr = bamblrSource.bamblrURI;
}

BamblrFeatureSource.prototype.getScales = function() {
    return [];
}

BamblrFeatureSource.prototype.getStyleSheet = function(callback) {
    var stylesheet = new DASStylesheet();

    var densStyle = new DASStyle();
    densStyle.glyph = 'HISTOGRAM';
    densStyle.COLOR1 = 'black';
    densStyle.COLOR2 = 'red';
    densStyle.HEIGHT=30;
    stylesheet.pushStyle({type: 'default'}, null, densStyle);

    return callback(stylesheet);
}

BamblrFeatureSource.prototype.fetch = function(chr, min, max, scale, types, pool, callback) {
    var rez = scale|0;
    if (rez < 1) {
        rez = 1;
    }
    var url = this.bamblr + '?seq=' + chr + '&min=' + min + '&max=' + max + '&rez=' + rez;
    new URLFetchable(url).fetch(function(data) {
        if (data == null) {
            dlog('failing bamblr');
            return;
        } else {
            var id = new Int32Array(data);
            var features = [];
            for (var ri = 0; ri < id.length; ++ri) {
                var f = new DASFeature();
                f.min = min + (ri * rez)
                f.max = f.min + rez - 1;
                f.segment = chr;
                f.type = 'bamblr';
                f.score = id[ri];
                features.push(f);
            }
            callback(null, features, rez);
            return;
        }
    });
}

function BAMFeatureSource(bamSource) {
    var thisB = this;
    this.bamSource = bamSource;
    this.opts = {credentials: bamSource.credentials, preflight: bamSource.preflight};
    this.bamHolder = new Awaited();
    
    if (this.opts.preflight) {
        var pfs = bwg_preflights[this.opts.preflight];
        if (!pfs) {
            pfs = new Awaited();
            bwg_preflights[this.opts.preflight] = pfs;

            var req = new XMLHttpRequest();
            req.onreadystatechange = function() {
                if (req.readyState == 4) {
                    if (req.status == 200) {
                        pfs.provide('success');
                    } else {
                        pfs.provide('failure');
                    }
                }
            };
            // req.setRequestHeader('cache-control', 'no-cache');    /* Doesn't work, not an allowed request header in CORS */
            req.open('get', this.opts.preflight + '?' + hex_sha1('salt' + Date.now()), true);    // Instead, ensure we always preflight a unique URI.
            if (this.opts.credentials) {
                req.withCredentials = 'true';
            }
            req.send('');
        }
        pfs.await(function(status) {
            if (status === 'success') {
                thisB.init();
            }
        });
    } else {
        thisB.init();
    }
}

BAMFeatureSource.prototype.init = function() {
    var thisB = this;
    var bamF, baiF;
    if (this.bamSource.bamBlob) {
        bamF = new BlobFetchable(this.bamSource.bamBlob);
        baiF = new BlobFetchable(this.bamSource.baiBlob);
    } else {
        bamF = new URLFetchable(this.bamSource.bamURI, {credentials: this.opts.credentials});
        baiF = new URLFetchable(this.bamSource.baiURI || (this.bamSource.bamURI + '.bai'), {credentials: this.opts.credentials});
    }
    makeBam(bamF, baiF, function(bam) {
        thisB.bamHolder.provide(bam);
    });
}

BAMFeatureSource.prototype.fetch = function(chr, min, max, scale, types, pool, callback) {
    var thisB = this;
    this.bamHolder.await(function(bam) {
        bam.fetch(chr, min, max, function(bamRecords, error) {
            if (error) {
                callback(error, null, null);
            } else {
                var features = [];
                for (var ri = 0; ri < bamRecords.length; ++ri) {
                    var r = bamRecords[ri];
                    var f = new DASFeature();
                    f.min = r.pos + 1;
                    f.max = r.pos + r.seq.length;
                    f.segment = r.segment;
                    f.type = 'bam';
                    f.id = r.readName;
                    f.notes = ['Sequence=' + r.seq, 'CIGAR=' + r.cigar, 'MQ=' + r.mq];
                    f.seq = r.seq;
                    features.push(f);
                }
                callback(null, features, 1000000000);
            }
        });
    });
}

BAMFeatureSource.prototype.getScales = function() {
    return 1000000000;
}

BAMFeatureSource.prototype.getStyleSheet = function(callback) {
    this.bamHolder.await(function(bam) {
	var stylesheet = new DASStylesheet();
                
        var densStyle = new DASStyle();
        densStyle.glyph = 'HISTOGRAM';
        densStyle.COLOR1 = 'black';
        densStyle.COLOR2 = 'red';
        densStyle.HEIGHT=30;
        stylesheet.pushStyle({type: 'density'}, 'low', densStyle);
        stylesheet.pushStyle({type: 'density'}, 'medium', densStyle);

        var wigStyle = new DASStyle();
        wigStyle.glyph = '__SEQUENCE';
        wigStyle.FGCOLOR = 'black';
        wigStyle.BGCOLOR = 'blue'
        wigStyle.HEIGHT = 8;
        wigStyle.BUMP = true;
        wigStyle.LABEL = false;
        wigStyle.ZINDEX = 20;
        stylesheet.pushStyle({type: 'bam'}, 'high', wigStyle);
	//                thisTier.stylesheet.pushStyle({type: 'bam'}, 'medium', wigStyle);

	return callback(stylesheet);
    });
}

function MappedFeatureSource(source, mapping) {
    this.source = source;
    this.mapping = mapping;
}

MappedFeatureSource.prototype.getStyleSheet = function(callback) {
    return this.source.getStyleSheet(callback);
}

MappedFeatureSource.prototype.getScales = function() {
    return this.source.getScales();
}

MappedFeatureSource.prototype.fetch = function(chr, min, max, scale, types, pool, callback) {
    var thisB = this;

    this.mapping.sourceBlocksForRange(chr, min, max, function(mseg) {
        if (mseg.length == 0) {
            callback("No mapping available for this regions", [], scale);
        } else {
            var seg = mseg[0];
            thisB.source.fetch(seg.name, seg.start, seg.end, scale, types, pool, function(status, features, fscale) {
                var mappedFeatures = [];
                if (features) {
                    for (var fi = 0; fi < features.length; ++fi) {
                        var f = features[fi];
                        var sn = f.segment;
                        if (sn.indexOf('chr') == 0) {
                            sn = sn.substr(3);
                        }
                        var mmin = thisB.mapping.mapPoint(sn, f.min);
                        var mmax = thisB.mapping.mapPoint(sn, f.max);
                        if (!mmin || !mmax || mmin.seq != mmax.seq || mmin.seq != chr) {
                            // Discard feature.
                            // dlog('discarding ' + miniJSONify(f));
                            if (f.parts && f.parts.length > 0) {    // FIXME: Ugly hack to make ASTD source map properly.
                                 mappedFeatures.push(f);
                            }
                        } else {
                            f.segment = mmin.seq;
                            f.min = mmin.pos;
                            f.max = mmax.pos;
                            if (f.min > f.max) {
                                var tmp = f.max;
                                f.max = f.min;
                                f.min = tmp;
                            }
                            if (mmin.flipped) {
                                if (f.orientation == '-') {
                                    f.orientation = '+';
                                } else if (f.orientation == '+') {
                                    f.orientation = '-';
                                }
                            }
                            mappedFeatures.push(f);
                        }
                    }
                }

                callback(status, mappedFeatures, fscale);
            });
        }
    });
}

function DummyFeatureSource() {
}

DummyFeatureSource.prototype.getScales = function() {
    return null;
}

DummyFeatureSource.prototype.fetch = function(chr, min, max, scale, types, pool, cnt) {
    return cnt(null, [], 1000000000);
}

DummyFeatureSource.prototype.getStyleSheet = function(callback) {
    var stylesheet = new DASStylesheet();
    var defStyle = new DASStyle();
    defStyle.glyph = 'BOX';
    defStyle.BGCOLOR = 'blue';
    defStyle.FGCOLOR = 'black';
    stylesheet.pushStyle({type: 'default'}, null, defStyle);
    return callback(stylesheet);
}

function DummySequenceSource() {
}

DummySequenceSource.prototype.fetch = function(chr, min, max, pool, cnt) {
    return cnt(null, null);
}

function JBrowseFeatureSource(source) {
    this.store = new JBrowseStore(source.jbURI, source.jbQuery);
}

JBrowseFeatureSource.prototype.getScales = function() {
    return null;
}

JBrowseFeatureSource.prototype.getStyleSheet = function(callback) {
    var stylesheet = new DASStylesheet();
    var wigStyle = new DASStyle();
    wigStyle.glyph = 'BOX';
    wigStyle.FGCOLOR = 'black';
    wigStyle.BGCOLOR = 'green'
    wigStyle.HEIGHT = 8;
    wigStyle.BUMP = true;
    wigStyle.LABEL = true;
    wigStyle.ZINDEX = 20;
    stylesheet.pushStyle({type: 'default'}, null, wigStyle);

    return callback(stylesheet);
}

JBrowseFeatureSource.prototype.fetch = function(chr, min, max, scale, types, pool, callback) {
    if (types && types.length == 0) {
        callback(null, [], scale);
        return;
    }
    
    var fops = {};

    this.store.features(
        new DASSegment(chr, min, max),
        fops,
        function(features, status) {
            callback(status, features, 100000);
        }
    );
}

function sourceAdapterIsCapable(s, cap) {
    if (!s.capabilities)
        return false;
    else return s.capabilities()[cap];
}
/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2013
//
// jbjson.js -- query JBrowse-style REST data stores
//

function JBrowseStore(base, query) {
    this.base = base;
    this.query = query;
}

var topLevelResp;

JBrowseStore.prototype.features = function(segment, opts, callback) {
    opts = opts || {};

    url = this.base + '/features/' + segment.name;

    var filters = [];
    if (this.query) {
	filters.push(this.query);
    }
    if (segment.isBounded) {
	filters.push('start=' + segment.start);
	filters.push('end=' + segment.end);
    }
    if (filters.length > 0) {
	url = url + '?' + filters.join('&');
    }

    var req = new XMLHttpRequest();
    req.onreadystatechange = function() {
	if (req.readyState == 4) {
	    if (req.status >= 300) {
		callback(null, 'Error code ' + req.status);
	    } else {
		var jf = JSON.parse(req.response)['features'];
		var features = [];
		for (fi = 0; fi < jf.length; ++fi) {
		    var j = jf[fi];
		    
		    var f = new DASFeature();
		    f.segment = segment;
		    f.min = (j['start'] | 0) + 1;
		    f.max = j['end'] | 0;
		    if (j.name) {
			f.label = j.name;
		    }
		    f.type = j.type || 'unknown';
		    
		    features.push(f);
		}
		callback(features);
	    }
	}
	
    };
    
    req.open('GET', url, true);
    req.responseType = 'text';
    req.send('');
}
/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2013
//
// ensembljson.js -- query the Ensembl REST API.
//

function EnsemblFeatureSource(source) {
    this.source = source;
    this.base = source.uri || 'http://beta.rest.ensembl.org';
    this.species = source.species || 'human';

    if (typeof source.type === 'string') {
        this.type = [source.type];
    } else {
        this.type = source.type || ['regulatory'];
    }
}

EnsemblFeatureSource.prototype.getStyleSheet = function(callback) {
    var stylesheet = new DASStylesheet();

    var tsStyle = new DASStyle();
    tsStyle.glyph = '__NONE';
    if (this.type.indexOf('exon') >= 0)
        stylesheet.pushStyle({type: 'transcript'}, null, tsStyle);
    if (this.type.indexOf('exon') >= 0 || this.type.indexOf('transcript') >= 0)
        stylesheet.pushStyle({type: 'gene'}, null, tsStyle);

    var cdsStyle = new DASStyle();
    cdsStyle.glyph = 'BOX';
    cdsStyle.FGCOLOR = 'black';
    cdsStyle.BGCOLOR = 'red'
    cdsStyle.HEIGHT = 8;
    cdsStyle.BUMP = true;
    cdsStyle.LABEL = true;
    cdsStyle.ZINDEX = 10;
    stylesheet.pushStyle({type: 'cds'}, null, cdsStyle);

    var wigStyle = new DASStyle();
    wigStyle.glyph = 'BOX';
    wigStyle.FGCOLOR = 'black';
    wigStyle.BGCOLOR = 'orange'
    wigStyle.HEIGHT = 8;
    wigStyle.BUMP = true;
    wigStyle.LABEL = true;
    wigStyle.ZINDEX = 20;
    stylesheet.pushStyle({type: 'default'}, null, wigStyle);

    return callback(stylesheet);
}


EnsemblFeatureSource.prototype.getScales = function() {
    return [];
}

EnsemblFeatureSource.prototype.fetch = function(chr, min, max, scale, types, pool, callback) {
    url = this.base + '/feature/region/' + this.species + '/' + chr + ':' + min + '-' + max;

    var filters = [];
    for (var ti = 0; ti < this.type.length; ++ti) {
        filters.push('feature=' + this.type[ti]);
    }
    filters.push('content-type=application/json');
    url = url + '?' + filters.join(';');
    
    console.log(url);

    var req = new XMLHttpRequest();
    req.onreadystatechange = function() {
	if (req.readyState == 4) {
	    if (req.status >= 300) {
		callback('Error code ' + req.status, null);
	    } else {
		var jf = JSON.parse(req.response);
		var features = [];
		for (fi = 0; fi < jf.length; ++fi) {
		    var j = jf[fi];
		    
		    var f = new DASFeature();
		    f.segment = chr;
		    f.min = j['start'] | 0;
		    f.max = j['end'] | 0;
		    f.type = j.feature_type || 'unknown';
		    f.id = j.ID;

                    if (j.Parent) {
                        var grp = new DASGroup();
                        grp.id = j.Parent;
                        f.groups = [grp];
                    }

                    if (j.strand) {
                        if (j.strand < 0) 
                            f.orientation = '-';
                        else if (j.strand > 0) 
                            f.orientation = '+';
                    }
		    
		    features.push(f);
		}
		callback(null, features);
	    }
	}
	
    };
    
    req.open('GET', url, true);
    req.responseType = 'text';
    req.send('');
}
/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Dalliance Genome Explorer
// (c) Thomas Down 2006-2013
//
// overlay.js: featuresources composed from multiple underlying sources
//

function OverlayFeatureSource(sources, opts) {
    this.sources = sources;
    this.opts = opts || {};

    if (opts.merge == 'concat') {
        this.merge = OverlayFeatureSource_merge_concat;
    } else {
        this.merge = OverlayFeatureSource_merge_byKey;
    }
}

OverlayFeatureSource.prototype.getScales = function() {
    return this.sources[0].getScales();
}

OverlayFeatureSource.prototype.getStyleSheet = function(callback) {
    return this.sources[0].getStyleSheet(callback);
}

OverlayFeatureSource.prototype.capabilities = function() {
    var s0 = this.sources[0];
    if (s0.capabilities) 
        return s0.capabilities();
    return {};
}

OverlayFeatureSource.prototype.fetch = function(chr, min, max, scale, types, pool, callback) {
    var baton = new OverlayBaton(this, callback, this.sources.length);
    for (var si = 0; si < this.sources.length; ++si) {
	this.fetchN(baton, si, chr, min, max, scale, types, pool);
    }
}

OverlayFeatureSource.prototype.fetchN = function(baton, si, chr, min, max, scale, types, pool) {
    this.sources[si].fetch(chr, min, max, scale, types, pool, function(status, features, scale) {
	return baton.completed(si, status, features, scale);
    });
}

OverlayFeatureSource.prototype.quantFindNextFeature = function(chr, pos, dir, threshold, callback) {
    return this.sources[0].quantFindNextFeature(chr, pos, dir, threshold, callback);
}

OverlayFeatureSource.prototype.findNextFeature = function(chr, pos, dir, callback) {
    return this.sources[0].findNextFeature(chr, pos, dir, callback);
}

function OverlayBaton(source, callback, count) {
    this.source = source;
    this.callback = callback;
    this.count = count;

    this.returnCount = 0;
    this.statusCount = 0;
    this.returns = [];
    this.features = []
    this.statuses = [];
    this.scale = null;
}

OverlayBaton.prototype.completed = function(index, status, features, scale) {
    if (this.scale == null || index == 0) 
	this.scale = scale;

    if (this.returns[index])
	throw 'Multiple returns for source ' + index;

    this.returns[index] = true;
    this.returnCount++;

    this.features[index] = features;

    if (status) {
	this.statuses[index] = status;
	this.statusCount++;
    }


    if (this.returnCount == this.count) {
	if (this.statusCount > 0) {
	    var message = '';
	    for (var si = 0; si < this.count; ++si) {
		var s = this.statuses[si];
		if (s != 0) {
		    if (message.length > 0) 
			message += ', ';
		    message += s;
		}
	    }
	    return this.callback(message, null, this.scale);
	} else {
	    this.callback(null, this.source.merge(this.features), this.scale);
	}
    }
}

OverlayFeatureSource.prototype.keyForFeature = function(feature) {
    return '' + feature.min + '..' + feature.max;
}

function OverlayFeatureSource_merge_byKey(featureSets) {
    var om = {};
    var of = featureSets[1];
    for (var fi = 0; fi < of.length; ++fi) {
	om[this.keyForFeature(of[fi])] = of[fi];
    }

    var mf = [];
    var fl = featureSets[0];
    for (var fi = 0; fi < fl.length; ++fi) {
	var f = fl[fi];
	of = om[this.keyForFeature(f)]
	if (of) {
	    if (of.id)
		f.id = of.id;
	    if (of.label) 
		f.label = of.label;
	}
	mf.push(f);
    }
    return mf;
}

function OverlayFeatureSource_merge_concat(featureSets) {
    var features = [];
    for (var fsi = 0; fsi < featureSets.length; ++fsi) {
        var fs = featureSets[fsi];
        var name = this.sources[fsi].name;
        for (var fi = 0; fi < fs.length; ++fi) {
            var f = fs[fi];
            f.method = name;
            features.push(f);
        }
    }
    return features;
}
/* -*- mode: javascript; c-basic-offset: 4; indent-tabs-mode: nil -*- */

// 
// Javascript ZLib
// By Thomas Down 2010-2011
//
// Based very heavily on portions of jzlib (by ymnk@jcraft.com), who in
// turn credits Jean-loup Gailly and Mark Adler for the original zlib code.
//
// inflate.js: ZLib inflate code
//

//
// Shared constants
//

var MAX_WBITS=15; // 32K LZ77 window
var DEF_WBITS=MAX_WBITS;
var MAX_MEM_LEVEL=9;
var MANY=1440;
var BMAX = 15;

// preset dictionary flag in zlib header
var PRESET_DICT=0x20;

var Z_NO_FLUSH=0;
var Z_PARTIAL_FLUSH=1;
var Z_SYNC_FLUSH=2;
var Z_FULL_FLUSH=3;
var Z_FINISH=4;

var Z_DEFLATED=8;

var Z_OK=0;
var Z_STREAM_END=1;
var Z_NEED_DICT=2;
var Z_ERRNO=-1;
var Z_STREAM_ERROR=-2;
var Z_DATA_ERROR=-3;
var Z_MEM_ERROR=-4;
var Z_BUF_ERROR=-5;
var Z_VERSION_ERROR=-6;

var METHOD=0;   // waiting for method byte
var FLAG=1;     // waiting for flag byte
var DICT4=2;    // four dictionary check bytes to go
var DICT3=3;    // three dictionary check bytes to go
var DICT2=4;    // two dictionary check bytes to go
var DICT1=5;    // one dictionary check byte to go
var DICT0=6;    // waiting for inflateSetDictionary
var BLOCKS=7;   // decompressing blocks
var CHECK4=8;   // four check bytes to go
var CHECK3=9;   // three check bytes to go
var CHECK2=10;  // two check bytes to go
var CHECK1=11;  // one check byte to go
var DONE=12;    // finished check, done
var BAD=13;     // got an error--stay here

var inflate_mask = [0x00000000, 0x00000001, 0x00000003, 0x00000007, 0x0000000f, 0x0000001f, 0x0000003f, 0x0000007f, 0x000000ff, 0x000001ff, 0x000003ff, 0x000007ff, 0x00000fff, 0x00001fff, 0x00003fff, 0x00007fff, 0x0000ffff];

var IB_TYPE=0;  // get type bits (3, including end bit)
var IB_LENS=1;  // get lengths for stored
var IB_STORED=2;// processing stored block
var IB_TABLE=3; // get table lengths
var IB_BTREE=4; // get bit lengths tree for a dynamic block
var IB_DTREE=5; // get length, distance trees for a dynamic block
var IB_CODES=6; // processing fixed or dynamic block
var IB_DRY=7;   // output remaining window bytes
var IB_DONE=8;  // finished last block, done
var IB_BAD=9;   // ot a data error--stuck here

var fixed_bl = 9;
var fixed_bd = 5;

var fixed_tl = [
    96,7,256, 0,8,80, 0,8,16, 84,8,115,
    82,7,31, 0,8,112, 0,8,48, 0,9,192,
    80,7,10, 0,8,96, 0,8,32, 0,9,160,
    0,8,0, 0,8,128, 0,8,64, 0,9,224,
    80,7,6, 0,8,88, 0,8,24, 0,9,144,
    83,7,59, 0,8,120, 0,8,56, 0,9,208,
    81,7,17, 0,8,104, 0,8,40, 0,9,176,
    0,8,8, 0,8,136, 0,8,72, 0,9,240,
    80,7,4, 0,8,84, 0,8,20, 85,8,227,
    83,7,43, 0,8,116, 0,8,52, 0,9,200,
    81,7,13, 0,8,100, 0,8,36, 0,9,168,
    0,8,4, 0,8,132, 0,8,68, 0,9,232,
    80,7,8, 0,8,92, 0,8,28, 0,9,152,
    84,7,83, 0,8,124, 0,8,60, 0,9,216,
    82,7,23, 0,8,108, 0,8,44, 0,9,184,
    0,8,12, 0,8,140, 0,8,76, 0,9,248,
    80,7,3, 0,8,82, 0,8,18, 85,8,163,
    83,7,35, 0,8,114, 0,8,50, 0,9,196,
    81,7,11, 0,8,98, 0,8,34, 0,9,164,
    0,8,2, 0,8,130, 0,8,66, 0,9,228,
    80,7,7, 0,8,90, 0,8,26, 0,9,148,
    84,7,67, 0,8,122, 0,8,58, 0,9,212,
    82,7,19, 0,8,106, 0,8,42, 0,9,180,
    0,8,10, 0,8,138, 0,8,74, 0,9,244,
    80,7,5, 0,8,86, 0,8,22, 192,8,0,
    83,7,51, 0,8,118, 0,8,54, 0,9,204,
    81,7,15, 0,8,102, 0,8,38, 0,9,172,
    0,8,6, 0,8,134, 0,8,70, 0,9,236,
    80,7,9, 0,8,94, 0,8,30, 0,9,156,
    84,7,99, 0,8,126, 0,8,62, 0,9,220,
    82,7,27, 0,8,110, 0,8,46, 0,9,188,
    0,8,14, 0,8,142, 0,8,78, 0,9,252,
    96,7,256, 0,8,81, 0,8,17, 85,8,131,
    82,7,31, 0,8,113, 0,8,49, 0,9,194,
    80,7,10, 0,8,97, 0,8,33, 0,9,162,
    0,8,1, 0,8,129, 0,8,65, 0,9,226,
    80,7,6, 0,8,89, 0,8,25, 0,9,146,
    83,7,59, 0,8,121, 0,8,57, 0,9,210,
    81,7,17, 0,8,105, 0,8,41, 0,9,178,
    0,8,9, 0,8,137, 0,8,73, 0,9,242,
    80,7,4, 0,8,85, 0,8,21, 80,8,258,
    83,7,43, 0,8,117, 0,8,53, 0,9,202,
    81,7,13, 0,8,101, 0,8,37, 0,9,170,
    0,8,5, 0,8,133, 0,8,69, 0,9,234,
    80,7,8, 0,8,93, 0,8,29, 0,9,154,
    84,7,83, 0,8,125, 0,8,61, 0,9,218,
    82,7,23, 0,8,109, 0,8,45, 0,9,186,
    0,8,13, 0,8,141, 0,8,77, 0,9,250,
    80,7,3, 0,8,83, 0,8,19, 85,8,195,
    83,7,35, 0,8,115, 0,8,51, 0,9,198,
    81,7,11, 0,8,99, 0,8,35, 0,9,166,
    0,8,3, 0,8,131, 0,8,67, 0,9,230,
    80,7,7, 0,8,91, 0,8,27, 0,9,150,
    84,7,67, 0,8,123, 0,8,59, 0,9,214,
    82,7,19, 0,8,107, 0,8,43, 0,9,182,
    0,8,11, 0,8,139, 0,8,75, 0,9,246,
    80,7,5, 0,8,87, 0,8,23, 192,8,0,
    83,7,51, 0,8,119, 0,8,55, 0,9,206,
    81,7,15, 0,8,103, 0,8,39, 0,9,174,
    0,8,7, 0,8,135, 0,8,71, 0,9,238,
    80,7,9, 0,8,95, 0,8,31, 0,9,158,
    84,7,99, 0,8,127, 0,8,63, 0,9,222,
    82,7,27, 0,8,111, 0,8,47, 0,9,190,
    0,8,15, 0,8,143, 0,8,79, 0,9,254,
    96,7,256, 0,8,80, 0,8,16, 84,8,115,
    82,7,31, 0,8,112, 0,8,48, 0,9,193,

    80,7,10, 0,8,96, 0,8,32, 0,9,161,
    0,8,0, 0,8,128, 0,8,64, 0,9,225,
    80,7,6, 0,8,88, 0,8,24, 0,9,145,
    83,7,59, 0,8,120, 0,8,56, 0,9,209,
    81,7,17, 0,8,104, 0,8,40, 0,9,177,
    0,8,8, 0,8,136, 0,8,72, 0,9,241,
    80,7,4, 0,8,84, 0,8,20, 85,8,227,
    83,7,43, 0,8,116, 0,8,52, 0,9,201,
    81,7,13, 0,8,100, 0,8,36, 0,9,169,
    0,8,4, 0,8,132, 0,8,68, 0,9,233,
    80,7,8, 0,8,92, 0,8,28, 0,9,153,
    84,7,83, 0,8,124, 0,8,60, 0,9,217,
    82,7,23, 0,8,108, 0,8,44, 0,9,185,
    0,8,12, 0,8,140, 0,8,76, 0,9,249,
    80,7,3, 0,8,82, 0,8,18, 85,8,163,
    83,7,35, 0,8,114, 0,8,50, 0,9,197,
    81,7,11, 0,8,98, 0,8,34, 0,9,165,
    0,8,2, 0,8,130, 0,8,66, 0,9,229,
    80,7,7, 0,8,90, 0,8,26, 0,9,149,
    84,7,67, 0,8,122, 0,8,58, 0,9,213,
    82,7,19, 0,8,106, 0,8,42, 0,9,181,
    0,8,10, 0,8,138, 0,8,74, 0,9,245,
    80,7,5, 0,8,86, 0,8,22, 192,8,0,
    83,7,51, 0,8,118, 0,8,54, 0,9,205,
    81,7,15, 0,8,102, 0,8,38, 0,9,173,
    0,8,6, 0,8,134, 0,8,70, 0,9,237,
    80,7,9, 0,8,94, 0,8,30, 0,9,157,
    84,7,99, 0,8,126, 0,8,62, 0,9,221,
    82,7,27, 0,8,110, 0,8,46, 0,9,189,
    0,8,14, 0,8,142, 0,8,78, 0,9,253,
    96,7,256, 0,8,81, 0,8,17, 85,8,131,
    82,7,31, 0,8,113, 0,8,49, 0,9,195,
    80,7,10, 0,8,97, 0,8,33, 0,9,163,
    0,8,1, 0,8,129, 0,8,65, 0,9,227,
    80,7,6, 0,8,89, 0,8,25, 0,9,147,
    83,7,59, 0,8,121, 0,8,57, 0,9,211,
    81,7,17, 0,8,105, 0,8,41, 0,9,179,
    0,8,9, 0,8,137, 0,8,73, 0,9,243,
    80,7,4, 0,8,85, 0,8,21, 80,8,258,
    83,7,43, 0,8,117, 0,8,53, 0,9,203,
    81,7,13, 0,8,101, 0,8,37, 0,9,171,
    0,8,5, 0,8,133, 0,8,69, 0,9,235,
    80,7,8, 0,8,93, 0,8,29, 0,9,155,
    84,7,83, 0,8,125, 0,8,61, 0,9,219,
    82,7,23, 0,8,109, 0,8,45, 0,9,187,
    0,8,13, 0,8,141, 0,8,77, 0,9,251,
    80,7,3, 0,8,83, 0,8,19, 85,8,195,
    83,7,35, 0,8,115, 0,8,51, 0,9,199,
    81,7,11, 0,8,99, 0,8,35, 0,9,167,
    0,8,3, 0,8,131, 0,8,67, 0,9,231,
    80,7,7, 0,8,91, 0,8,27, 0,9,151,
    84,7,67, 0,8,123, 0,8,59, 0,9,215,
    82,7,19, 0,8,107, 0,8,43, 0,9,183,
    0,8,11, 0,8,139, 0,8,75, 0,9,247,
    80,7,5, 0,8,87, 0,8,23, 192,8,0,
    83,7,51, 0,8,119, 0,8,55, 0,9,207,
    81,7,15, 0,8,103, 0,8,39, 0,9,175,
    0,8,7, 0,8,135, 0,8,71, 0,9,239,
    80,7,9, 0,8,95, 0,8,31, 0,9,159,
    84,7,99, 0,8,127, 0,8,63, 0,9,223,
    82,7,27, 0,8,111, 0,8,47, 0,9,191,
    0,8,15, 0,8,143, 0,8,79, 0,9,255
];
var fixed_td = [
    80,5,1, 87,5,257, 83,5,17, 91,5,4097,
    81,5,5, 89,5,1025, 85,5,65, 93,5,16385,
    80,5,3, 88,5,513, 84,5,33, 92,5,8193,
    82,5,9, 90,5,2049, 86,5,129, 192,5,24577,
    80,5,2, 87,5,385, 83,5,25, 91,5,6145,
    81,5,7, 89,5,1537, 85,5,97, 93,5,24577,
    80,5,4, 88,5,769, 84,5,49, 92,5,12289,
    82,5,13, 90,5,3073, 86,5,193, 192,5,24577
];

  // Tables for deflate from PKZIP's appnote.txt.
  var cplens = [ // Copy lengths for literal codes 257..285
        3, 4, 5, 6, 7, 8, 9, 10, 11, 13, 15, 17, 19, 23, 27, 31,
        35, 43, 51, 59, 67, 83, 99, 115, 131, 163, 195, 227, 258, 0, 0
  ];

  // see note #13 above about 258
  var cplext = [ // Extra bits for literal codes 257..285
        0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2,
        3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5, 0, 112, 112  // 112==invalid
  ];

 var cpdist = [ // Copy offsets for distance codes 0..29
        1, 2, 3, 4, 5, 7, 9, 13, 17, 25, 33, 49, 65, 97, 129, 193,
        257, 385, 513, 769, 1025, 1537, 2049, 3073, 4097, 6145,
        8193, 12289, 16385, 24577
  ];

  var cpdext = [ // Extra bits for distance codes
        0, 0, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6,
        7, 7, 8, 8, 9, 9, 10, 10, 11, 11,
        12, 12, 13, 13];

//
// ZStream.java
//

function ZStream() {
}


ZStream.prototype.inflateInit = function(w, nowrap) {
    if (!w) {
	w = DEF_WBITS;
    }
    if (nowrap) {
	nowrap = false;
    }
    this.istate = new Inflate();
    return this.istate.inflateInit(this, nowrap?-w:w);
}

ZStream.prototype.inflate = function(f) {
    if(this.istate==null) return Z_STREAM_ERROR;
    return this.istate.inflate(this, f);
}

ZStream.prototype.inflateEnd = function(){
    if(this.istate==null) return Z_STREAM_ERROR;
    var ret=istate.inflateEnd(this);
    this.istate = null;
    return ret;
}
ZStream.prototype.inflateSync = function(){
    // if(istate == null) return Z_STREAM_ERROR;
    return istate.inflateSync(this);
}
ZStream.prototype.inflateSetDictionary = function(dictionary, dictLength){
    // if(istate == null) return Z_STREAM_ERROR;
    return istate.inflateSetDictionary(this, dictionary, dictLength);
}

/*

  public int deflateInit(int level){
    return deflateInit(level, MAX_WBITS);
  }
  public int deflateInit(int level, boolean nowrap){
    return deflateInit(level, MAX_WBITS, nowrap);
  }
  public int deflateInit(int level, int bits){
    return deflateInit(level, bits, false);
  }
  public int deflateInit(int level, int bits, boolean nowrap){
    dstate=new Deflate();
    return dstate.deflateInit(this, level, nowrap?-bits:bits);
  }
  public int deflate(int flush){
    if(dstate==null){
      return Z_STREAM_ERROR;
    }
    return dstate.deflate(this, flush);
  }
  public int deflateEnd(){
    if(dstate==null) return Z_STREAM_ERROR;
    int ret=dstate.deflateEnd();
    dstate=null;
    return ret;
  }
  public int deflateParams(int level, int strategy){
    if(dstate==null) return Z_STREAM_ERROR;
    return dstate.deflateParams(this, level, strategy);
  }
  public int deflateSetDictionary (byte[] dictionary, int dictLength){
    if(dstate == null)
      return Z_STREAM_ERROR;
    return dstate.deflateSetDictionary(this, dictionary, dictLength);
  }

*/

/*
  // Flush as much pending output as possible. All deflate() output goes
  // through this function so some applications may wish to modify it
  // to avoid allocating a large strm->next_out buffer and copying into it.
  // (See also read_buf()).
  void flush_pending(){
    int len=dstate.pending;

    if(len>avail_out) len=avail_out;
    if(len==0) return;

    if(dstate.pending_buf.length<=dstate.pending_out ||
       next_out.length<=next_out_index ||
       dstate.pending_buf.length<(dstate.pending_out+len) ||
       next_out.length<(next_out_index+len)){
      System.out.println(dstate.pending_buf.length+", "+dstate.pending_out+
			 ", "+next_out.length+", "+next_out_index+", "+len);
      System.out.println("avail_out="+avail_out);
    }

    System.arraycopy(dstate.pending_buf, dstate.pending_out,
		     next_out, next_out_index, len);

    next_out_index+=len;
    dstate.pending_out+=len;
    total_out+=len;
    avail_out-=len;
    dstate.pending-=len;
    if(dstate.pending==0){
      dstate.pending_out=0;
    }
  }

  // Read a new buffer from the current input stream, update the adler32
  // and total number of bytes read.  All deflate() input goes through
  // this function so some applications may wish to modify it to avoid
  // allocating a large strm->next_in buffer and copying from it.
  // (See also flush_pending()).
  int read_buf(byte[] buf, int start, int size) {
    int len=avail_in;

    if(len>size) len=size;
    if(len==0) return 0;

    avail_in-=len;

    if(dstate.noheader==0) {
      adler=_adler.adler32(adler, next_in, next_in_index, len);
    }
    System.arraycopy(next_in, next_in_index, buf, start, len);
    next_in_index  += len;
    total_in += len;
    return len;
  }

  public void free(){
    next_in=null;
    next_out=null;
    msg=null;
    _adler=null;
  }
}
*/


//
// Inflate.java
//

function Inflate() {
    this.was = [0];
}

Inflate.prototype.inflateReset = function(z) {
    if(z == null || z.istate == null) return Z_STREAM_ERROR;
    
    z.total_in = z.total_out = 0;
    z.msg = null;
    z.istate.mode = z.istate.nowrap!=0 ? BLOCKS : METHOD;
    z.istate.blocks.reset(z, null);
    return Z_OK;
}

Inflate.prototype.inflateEnd = function(z){
    if(this.blocks != null)
      this.blocks.free(z);
    this.blocks=null;
    return Z_OK;
}

Inflate.prototype.inflateInit = function(z, w){
    z.msg = null;
    this.blocks = null;

    // handle undocumented nowrap option (no zlib header or check)
    nowrap = 0;
    if(w < 0){
      w = - w;
      nowrap = 1;
    }

    // set window size
    if(w<8 ||w>15){
      this.inflateEnd(z);
      return Z_STREAM_ERROR;
    }
    this.wbits=w;

    z.istate.blocks=new InfBlocks(z, 
				  z.istate.nowrap!=0 ? null : this,
				  1<<w);

    // reset state
    this.inflateReset(z);
    return Z_OK;
  }

Inflate.prototype.inflate = function(z, f){
    var r, b;

    if(z == null || z.istate == null || z.next_in == null)
      return Z_STREAM_ERROR;
    f = f == Z_FINISH ? Z_BUF_ERROR : Z_OK;
    r = Z_BUF_ERROR;
    while (true){
      switch (z.istate.mode){
      case METHOD:

        if(z.avail_in==0)return r;r=f;

        z.avail_in--; z.total_in++;
        if(((z.istate.method = z.next_in[z.next_in_index++])&0xf)!=Z_DEFLATED){
          z.istate.mode = BAD;
          z.msg="unknown compression method";
          z.istate.marker = 5;       // can't try inflateSync
          break;
        }
        if((z.istate.method>>4)+8>z.istate.wbits){
          z.istate.mode = BAD;
          z.msg="invalid window size";
          z.istate.marker = 5;       // can't try inflateSync
          break;
        }
        z.istate.mode=FLAG;
      case FLAG:

        if(z.avail_in==0)return r;r=f;

        z.avail_in--; z.total_in++;
        b = (z.next_in[z.next_in_index++])&0xff;

        if((((z.istate.method << 8)+b) % 31)!=0){
          z.istate.mode = BAD;
          z.msg = "incorrect header check";
          z.istate.marker = 5;       // can't try inflateSync
          break;
        }

        if((b&PRESET_DICT)==0){
          z.istate.mode = BLOCKS;
          break;
        }
        z.istate.mode = DICT4;
      case DICT4:

        if(z.avail_in==0)return r;r=f;

        z.avail_in--; z.total_in++;
        z.istate.need=((z.next_in[z.next_in_index++]&0xff)<<24)&0xff000000;
        z.istate.mode=DICT3;
      case DICT3:

        if(z.avail_in==0)return r;r=f;

        z.avail_in--; z.total_in++;
        z.istate.need+=((z.next_in[z.next_in_index++]&0xff)<<16)&0xff0000;
        z.istate.mode=DICT2;
      case DICT2:

        if(z.avail_in==0)return r;r=f;

        z.avail_in--; z.total_in++;
        z.istate.need+=((z.next_in[z.next_in_index++]&0xff)<<8)&0xff00;
        z.istate.mode=DICT1;
      case DICT1:

        if(z.avail_in==0)return r;r=f;

        z.avail_in--; z.total_in++;
        z.istate.need += (z.next_in[z.next_in_index++]&0xff);
        z.adler = z.istate.need;
        z.istate.mode = DICT0;
        return Z_NEED_DICT;
      case DICT0:
        z.istate.mode = BAD;
        z.msg = "need dictionary";
        z.istate.marker = 0;       // can try inflateSync
        return Z_STREAM_ERROR;
      case BLOCKS:

        r = z.istate.blocks.proc(z, r);
        if(r == Z_DATA_ERROR){
          z.istate.mode = BAD;
          z.istate.marker = 0;     // can try inflateSync
          break;
        }
        if(r == Z_OK){
          r = f;
        }
        if(r != Z_STREAM_END){
          return r;
        }
        r = f;
        z.istate.blocks.reset(z, z.istate.was);
        if(z.istate.nowrap!=0){
          z.istate.mode=DONE;
          break;
        }
        z.istate.mode=CHECK4;
      case CHECK4:

        if(z.avail_in==0)return r;r=f;

        z.avail_in--; z.total_in++;
        z.istate.need=((z.next_in[z.next_in_index++]&0xff)<<24)&0xff000000;
        z.istate.mode=CHECK3;
      case CHECK3:

        if(z.avail_in==0)return r;r=f;

        z.avail_in--; z.total_in++;
        z.istate.need+=((z.next_in[z.next_in_index++]&0xff)<<16)&0xff0000;
        z.istate.mode = CHECK2;
      case CHECK2:

        if(z.avail_in==0)return r;r=f;

        z.avail_in--; z.total_in++;
        z.istate.need+=((z.next_in[z.next_in_index++]&0xff)<<8)&0xff00;
        z.istate.mode = CHECK1;
      case CHECK1:

        if(z.avail_in==0)return r;r=f;

        z.avail_in--; z.total_in++;
        z.istate.need+=(z.next_in[z.next_in_index++]&0xff);

        if(((z.istate.was[0])) != ((z.istate.need))){
          z.istate.mode = BAD;
          z.msg = "incorrect data check";
          z.istate.marker = 5;       // can't try inflateSync
          break;
        }

        z.istate.mode = DONE;
      case DONE:
        return Z_STREAM_END;
      case BAD:
        return Z_DATA_ERROR;
      default:
        return Z_STREAM_ERROR;
      }
    }
  }


Inflate.prototype.inflateSetDictionary = function(z,  dictionary, dictLength) {
    var index=0;
    var length = dictLength;
    if(z==null || z.istate == null|| z.istate.mode != DICT0)
      return Z_STREAM_ERROR;

    if(z._adler.adler32(1, dictionary, 0, dictLength)!=z.adler){
      return Z_DATA_ERROR;
    }

    z.adler = z._adler.adler32(0, null, 0, 0);

    if(length >= (1<<z.istate.wbits)){
      length = (1<<z.istate.wbits)-1;
      index=dictLength - length;
    }
    z.istate.blocks.set_dictionary(dictionary, index, length);
    z.istate.mode = BLOCKS;
    return Z_OK;
  }

//  static private byte[] mark = {(byte)0, (byte)0, (byte)0xff, (byte)0xff};
var mark = [0, 0, 255, 255]

Inflate.prototype.inflateSync = function(z){
    var n;       // number of bytes to look at
    var p;       // pointer to bytes
    var m;       // number of marker bytes found in a row
    var r, w;   // temporaries to save total_in and total_out

    // set up
    if(z == null || z.istate == null)
      return Z_STREAM_ERROR;
    if(z.istate.mode != BAD){
      z.istate.mode = BAD;
      z.istate.marker = 0;
    }
    if((n=z.avail_in)==0)
      return Z_BUF_ERROR;
    p=z.next_in_index;
    m=z.istate.marker;

    // search
    while (n!=0 && m < 4){
      if(z.next_in[p] == mark[m]){
        m++;
      }
      else if(z.next_in[p]!=0){
        m = 0;
      }
      else{
        m = 4 - m;
      }
      p++; n--;
    }

    // restore
    z.total_in += p-z.next_in_index;
    z.next_in_index = p;
    z.avail_in = n;
    z.istate.marker = m;

    // return no joy or set up to restart on a new block
    if(m != 4){
      return Z_DATA_ERROR;
    }
    r=z.total_in;  w=z.total_out;
    this.inflateReset(z);
    z.total_in=r;  z.total_out = w;
    z.istate.mode = BLOCKS;
    return Z_OK;
}

  // Returns true if inflate is currently at the end of a block generated
  // by Z_SYNC_FLUSH or Z_FULL_FLUSH. This function is used by one PPP
  // implementation to provide an additional safety check. PPP uses Z_SYNC_FLUSH
  // but removes the length bytes of the resulting empty stored block. When
  // decompressing, PPP checks that at the end of input packet, inflate is
  // waiting for these length bytes.
Inflate.prototype.inflateSyncPoint = function(z){
    if(z == null || z.istate == null || z.istate.blocks == null)
      return Z_STREAM_ERROR;
    return z.istate.blocks.sync_point();
}


//
// InfBlocks.java
//

var INFBLOCKS_BORDER = [16, 17, 18, 0, 8, 7, 9, 6, 10, 5, 11, 4, 12, 3, 13, 2, 14, 1, 15];

function InfBlocks(z, checkfn, w) {
    this.hufts=new Int32Array(MANY*3);
    this.window=new Uint8Array(w);
    this.end=w;
    this.checkfn = checkfn;
    this.mode = IB_TYPE;
    this.reset(z, null);

    this.left = 0;            // if STORED, bytes left to copy 

    this.table = 0;           // table lengths (14 bits) 
    this.index = 0;           // index into blens (or border) 
    this.blens = null;         // bit lengths of codes 
    this.bb=new Int32Array(1); // bit length tree depth 
    this.tb=new Int32Array(1); // bit length decoding tree 

    this.codes = new InfCodes();

    this.last = 0;            // true if this block is the last block 

  // mode independent information 
    this.bitk = 0;            // bits in bit buffer 
    this.bitb = 0;            // bit buffer 
    this.read = 0;            // window read pointer 
    this.write = 0;           // window write pointer 
    this.check = 0;          // check on output 

    this.inftree=new InfTree();
}




InfBlocks.prototype.reset = function(z, c){
    if(c) c[0]=this.check;
    if(this.mode==IB_CODES){
      this.codes.free(z);
    }
    this.mode=IB_TYPE;
    this.bitk=0;
    this.bitb=0;
    this.read=this.write=0;

    if(this.checkfn)
      z.adler=this.check=z._adler.adler32(0, null, 0, 0);
  }

 InfBlocks.prototype.proc = function(z, r){
    var t;              // temporary storage
    var b;              // bit buffer
    var k;              // bits in bit buffer
    var p;              // input data pointer
    var n;              // bytes available there
    var q;              // output window write pointer
    var m;              // bytes to end of window or read pointer

    // copy input/output information to locals (UPDATE macro restores)
    {p=z.next_in_index;n=z.avail_in;b=this.bitb;k=this.bitk;}
    {q=this.write;m=(q<this.read ? this.read-q-1 : this.end-q);}

    // process input based on current state
    while(true){
      switch (this.mode){
      case IB_TYPE:

	while(k<(3)){
	  if(n!=0){
	    r=Z_OK;
	  }
	  else{
	    this.bitb=b; this.bitk=k; 
	    z.avail_in=n;
	    z.total_in+=p-z.next_in_index;z.next_in_index=p;
	    this.write=q;
	    return this.inflate_flush(z,r);
	  };
	  n--;
	  b|=(z.next_in[p++]&0xff)<<k;
	  k+=8;
	}
	t = (b & 7);
	this.last = t & 1;

	switch (t >>> 1){
        case 0:                         // stored 
          {b>>>=(3);k-=(3);}
          t = k & 7;                    // go to byte boundary

          {b>>>=(t);k-=(t);}
          this.mode = IB_LENS;                  // get length of stored block
          break;
        case 1:                         // fixed
          {
              var bl=new Int32Array(1);
	      var bd=new Int32Array(1);
              var tl=[];
	      var td=[];

	      inflate_trees_fixed(bl, bd, tl, td, z);
              this.codes.init(bl[0], bd[0], tl[0], 0, td[0], 0, z);
          }

          {b>>>=(3);k-=(3);}

          this.mode = IB_CODES;
          break;
        case 2:                         // dynamic

          {b>>>=(3);k-=(3);}

          this.mode = IB_TABLE;
          break;
        case 3:                         // illegal

          {b>>>=(3);k-=(3);}
          this.mode = BAD;
          z.msg = "invalid block type";
          r = Z_DATA_ERROR;

	  this.bitb=b; this.bitk=k; 
	  z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	  this.write=q;
	  return this.inflate_flush(z,r);
	}
	break;
      case IB_LENS:
	while(k<(32)){
	  if(n!=0){
	    r=Z_OK;
	  }
	  else{
	    this.bitb=b; this.bitk=k; 
	    z.avail_in=n;
	    z.total_in+=p-z.next_in_index;z.next_in_index=p;
	    this.write=q;
	    return this.inflate_flush(z,r);
	  };
	  n--;
	  b|=(z.next_in[p++]&0xff)<<k;
	  k+=8;
	}

	if ((((~b) >>> 16) & 0xffff) != (b & 0xffff)){
	  this.mode = BAD;
	  z.msg = "invalid stored block lengths";
	  r = Z_DATA_ERROR;

	  this.bitb=b; this.bitk=k; 
	  z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	  this.write=q;
	  return this.inflate_flush(z,r);
	}
	this.left = (b & 0xffff);
	b = k = 0;                       // dump bits
	this.mode = left!=0 ? IB_STORED : (this.last!=0 ? IB_DRY : IB_TYPE);
	break;
      case IB_STORED:
	if (n == 0){
	  this.bitb=b; this.bitk=k; 
	  z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	  write=q;
	  return this.inflate_flush(z,r);
	}

	if(m==0){
	  if(q==end&&read!=0){
	    q=0; m=(q<this.read ? this.read-q-1 : this.end-q);
	  }
	  if(m==0){
	    this.write=q; 
	    r=this.inflate_flush(z,r);
	    q=this.write; m = (q < this.read ? this.read-q-1 : this.end-q);
	    if(q==this.end && this.read != 0){
	      q=0; m = (q < this.read ? this.read-q-1 : this.end-q);
	    }
	    if(m==0){
	      this.bitb=b; this.bitk=k; 
	      z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	      this.write=q;
	      return this.inflate_flush(z,r);
	    }
	  }
	}
	r=Z_OK;

	t = this.left;
	if(t>n) t = n;
	if(t>m) t = m;
	arrayCopy(z.next_in, p, window, q, t);
	p += t;  n -= t;
	q += t;  m -= t;
	if ((this.left -= t) != 0)
	  break;
	this.mode = (this.last != 0 ? IB_DRY : IB_TYPE);
	break;
      case IB_TABLE:

	while(k<(14)){
	  if(n!=0){
	    r=Z_OK;
	  }
	  else{
	    this.bitb=b; this.bitk=k; 
	    z.avail_in=n;
	    z.total_in+=p-z.next_in_index;z.next_in_index=p;
	    this.write=q;
	    return this.inflate_flush(z,r);
	  };
	  n--;
	  b|=(z.next_in[p++]&0xff)<<k;
	  k+=8;
	}

	this.table = t = (b & 0x3fff);
	if ((t & 0x1f) > 29 || ((t >> 5) & 0x1f) > 29)
	  {
	    this.mode = IB_BAD;
	    z.msg = "too many length or distance symbols";
	    r = Z_DATA_ERROR;

	    this.bitb=b; this.bitk=k; 
	    z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	    this.write=q;
	    return this.inflate_flush(z,r);
	  }
	t = 258 + (t & 0x1f) + ((t >> 5) & 0x1f);
	if(this.blens==null || this.blens.length<t){
	    this.blens=new Int32Array(t);
	}
	else{
	  for(var i=0; i<t; i++){
              this.blens[i]=0;
          }
	}

	{b>>>=(14);k-=(14);}

	this.index = 0;
	mode = IB_BTREE;
      case IB_BTREE:
	while (this.index < 4 + (this.table >>> 10)){
	  while(k<(3)){
	    if(n!=0){
	      r=Z_OK;
	    }
	    else{
	      this.bitb=b; this.bitk=k; 
	      z.avail_in=n;
	      z.total_in+=p-z.next_in_index;z.next_in_index=p;
	      this.write=q;
	      return this.inflate_flush(z,r);
	    };
	    n--;
	    b|=(z.next_in[p++]&0xff)<<k;
	    k+=8;
	  }

	  this.blens[INFBLOCKS_BORDER[this.index++]] = b&7;

	  {b>>>=(3);k-=(3);}
	}

	while(this.index < 19){
	  this.blens[INFBLOCKS_BORDER[this.index++]] = 0;
	}

	this.bb[0] = 7;
	t = this.inftree.inflate_trees_bits(this.blens, this.bb, this.tb, this.hufts, z);
	if (t != Z_OK){
	  r = t;
	  if (r == Z_DATA_ERROR){
	    this.blens=null;
	    this.mode = IB_BAD;
	  }

	  this.bitb=b; this.bitk=k; 
	  z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	  write=q;
	  return this.inflate_flush(z,r);
	}

	this.index = 0;
	this.mode = IB_DTREE;
      case IB_DTREE:
	while (true){
	  t = this.table;
	  if(!(this.index < 258 + (t & 0x1f) + ((t >> 5) & 0x1f))){
	    break;
	  }

	  var h; //int[]
	  var i, j, c;

	  t = this.bb[0];

	  while(k<(t)){
	    if(n!=0){
	      r=Z_OK;
	    }
	    else{
	      this.bitb=b; this.bitk=k; 
	      z.avail_in=n;
	      z.total_in+=p-z.next_in_index;z.next_in_index=p;
	      this.write=q;
	      return this.inflate_flush(z,r);
	    };
	    n--;
	    b|=(z.next_in[p++]&0xff)<<k;
	    k+=8;
	  }

//	  if (this.tb[0]==-1){
//            dlog("null...");
//	  }

	  t=this.hufts[(this.tb[0]+(b & inflate_mask[t]))*3+1];
	  c=this.hufts[(this.tb[0]+(b & inflate_mask[t]))*3+2];

	  if (c < 16){
	    b>>>=(t);k-=(t);
	    this.blens[this.index++] = c;
	  }
	  else { // c == 16..18
	    i = c == 18 ? 7 : c - 14;
	    j = c == 18 ? 11 : 3;

	    while(k<(t+i)){
	      if(n!=0){
		r=Z_OK;
	      }
	      else{
		this.bitb=b; this.bitk=k; 
		z.avail_in=n;
		z.total_in+=p-z.next_in_index;z.next_in_index=p;
		this.write=q;
		return this.inflate_flush(z,r);
	      };
	      n--;
	      b|=(z.next_in[p++]&0xff)<<k;
	      k+=8;
	    }

	    b>>>=(t);k-=(t);

	    j += (b & inflate_mask[i]);

	    b>>>=(i);k-=(i);

	    i = this.index;
	    t = this.table;
	    if (i + j > 258 + (t & 0x1f) + ((t >> 5) & 0x1f) ||
		(c == 16 && i < 1)){
	      this.blens=null;
	      this.mode = IB_BAD;
	      z.msg = "invalid bit length repeat";
	      r = Z_DATA_ERROR;

	      this.bitb=b; this.bitk=k; 
	      z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	      this.write=q;
	      return this.inflate_flush(z,r);
	    }

	    c = c == 16 ? this.blens[i-1] : 0;
	    do{
	      this.blens[i++] = c;
	    }
	    while (--j!=0);
	    this.index = i;
	  }
	}

	this.tb[0]=-1;
	{
	    var bl=new Int32Array(1);
	    var bd=new Int32Array(1);
	    var tl=new Int32Array(1);
	    var td=new Int32Array(1);
	    bl[0] = 9;         // must be <= 9 for lookahead assumptions
	    bd[0] = 6;         // must be <= 9 for lookahead assumptions

	    t = this.table;
	    t = this.inftree.inflate_trees_dynamic(257 + (t & 0x1f), 
					      1 + ((t >> 5) & 0x1f),
					      this.blens, bl, bd, tl, td, this.hufts, z);

	    if (t != Z_OK){
	        if (t == Z_DATA_ERROR){
	            this.blens=null;
	            this.mode = BAD;
	        }
	        r = t;

	        this.bitb=b; this.bitk=k; 
	        z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	        this.write=q;
	        return this.inflate_flush(z,r);
	    }
	    this.codes.init(bl[0], bd[0], this.hufts, tl[0], this.hufts, td[0], z);
	}
	this.mode = IB_CODES;
      case IB_CODES:
	this.bitb=b; this.bitk=k;
	z.avail_in=n; z.total_in+=p-z.next_in_index;z.next_in_index=p;
	this.write=q;

	if ((r = this.codes.proc(this, z, r)) != Z_STREAM_END){
	  return this.inflate_flush(z, r);
	}
	r = Z_OK;
	this.codes.free(z);

	p=z.next_in_index; n=z.avail_in;b=this.bitb;k=this.bitk;
	q=this.write;m = (q < this.read ? this.read-q-1 : this.end-q);

	if (this.last==0){
	  this.mode = IB_TYPE;
	  break;
	}
	this.mode = IB_DRY;
      case IB_DRY:
	this.write=q; 
	r = this.inflate_flush(z, r); 
	q=this.write; m = (q < this.read ? this.read-q-1 : this.end-q);
	if (this.read != this.write){
	  this.bitb=b; this.bitk=k; 
	  z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	  this.write=q;
	  return this.inflate_flush(z, r);
	}
	mode = DONE;
      case IB_DONE:
	r = Z_STREAM_END;

	this.bitb=b; this.bitk=k; 
	z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	this.write=q;
	return this.inflate_flush(z, r);
      case IB_BAD:
	r = Z_DATA_ERROR;

	this.bitb=b; this.bitk=k; 
	z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	this.write=q;
	return this.inflate_flush(z, r);

      default:
	r = Z_STREAM_ERROR;

	this.bitb=b; this.bitk=k; 
	z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	this.write=q;
	return this.inflate_flush(z, r);
      }
    }
  }

InfBlocks.prototype.free = function(z){
    this.reset(z, null);
    this.window=null;
    this.hufts=null;
}

InfBlocks.prototype.set_dictionary = function(d, start, n){
    arrayCopy(d, start, window, 0, n);
    this.read = this.write = n;
}

  // Returns true if inflate is currently at the end of a block generated
  // by Z_SYNC_FLUSH or Z_FULL_FLUSH. 
InfBlocks.prototype.sync_point = function(){
    return this.mode == IB_LENS;
}

  // copy as much as possible from the sliding window to the output area
InfBlocks.prototype.inflate_flush = function(z, r){
    var n;
    var p;
    var q;

    // local copies of source and destination pointers
    p = z.next_out_index;
    q = this.read;

    // compute number of bytes to copy as far as end of window
    n = ((q <= this.write ? this.write : this.end) - q);
    if (n > z.avail_out) n = z.avail_out;
    if (n!=0 && r == Z_BUF_ERROR) r = Z_OK;

    // update counters
    z.avail_out -= n;
    z.total_out += n;

    // update check information
    if(this.checkfn != null)
      z.adler=this.check=z._adler.adler32(this.check, this.window, q, n);

    // copy as far as end of window
    arrayCopy(this.window, q, z.next_out, p, n);
    p += n;
    q += n;

    // see if more to copy at beginning of window
    if (q == this.end){
      // wrap pointers
      q = 0;
      if (this.write == this.end)
        this.write = 0;

      // compute bytes to copy
      n = this.write - q;
      if (n > z.avail_out) n = z.avail_out;
      if (n!=0 && r == Z_BUF_ERROR) r = Z_OK;

      // update counters
      z.avail_out -= n;
      z.total_out += n;

      // update check information
      if(this.checkfn != null)
	z.adler=this.check=z._adler.adler32(this.check, this.window, q, n);

      // copy
      arrayCopy(this.window, q, z.next_out, p, n);
      p += n;
      q += n;
    }

    // update pointers
    z.next_out_index = p;
    this.read = q;

    // done
    return r;
  }

//
// InfCodes.java
//

var IC_START=0;  // x: set up for LEN
var IC_LEN=1;    // i: get length/literal/eob next
var IC_LENEXT=2; // i: getting length extra (have base)
var IC_DIST=3;   // i: get distance next
var IC_DISTEXT=4;// i: getting distance extra
var IC_COPY=5;   // o: copying bytes in window, waiting for space
var IC_LIT=6;    // o: got literal, waiting for output space
var IC_WASH=7;   // o: got eob, possibly still output waiting
var IC_END=8;    // x: got eob and all data flushed
var IC_BADCODE=9;// x: got error

function InfCodes() {
}

InfCodes.prototype.init = function(bl, bd, tl, tl_index, td, td_index, z) {
    this.mode=IC_START;
    this.lbits=bl;
    this.dbits=bd;
    this.ltree=tl;
    this.ltree_index=tl_index;
    this.dtree = td;
    this.dtree_index=td_index;
    this.tree=null;
}

InfCodes.prototype.proc = function(s, z, r){ 
    var j;              // temporary storage
    var t;              // temporary pointer (int[])
    var tindex;         // temporary pointer
    var e;              // extra bits or operation
    var b=0;            // bit buffer
    var k=0;            // bits in bit buffer
    var p=0;            // input data pointer
    var n;              // bytes available there
    var q;              // output window write pointer
    var m;              // bytes to end of window or read pointer
    var f;              // pointer to copy strings from

    // copy input/output information to locals (UPDATE macro restores)
    p=z.next_in_index;n=z.avail_in;b=s.bitb;k=s.bitk;
    q=s.write;m=q<s.read?s.read-q-1:s.end-q;

    // process input and output based on current state
    while (true){
      switch (this.mode){
	// waiting for "i:"=input, "o:"=output, "x:"=nothing
      case IC_START:         // x: set up for LEN
	if (m >= 258 && n >= 10){

	  s.bitb=b;s.bitk=k;
	  z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	  s.write=q;
	  r = this.inflate_fast(this.lbits, this.dbits, 
			   this.ltree, this.ltree_index, 
			   this.dtree, this.dtree_index,
			   s, z);

	  p=z.next_in_index;n=z.avail_in;b=s.bitb;k=s.bitk;
	  q=s.write;m=q<s.read?s.read-q-1:s.end-q;

	  if (r != Z_OK){
	    this.mode = r == Z_STREAM_END ? IC_WASH : IC_BADCODE;
	    break;
	  }
	}
	this.need = this.lbits;
	this.tree = this.ltree;
	this.tree_index=this.ltree_index;

	this.mode = IC_LEN;
      case IC_LEN:           // i: get length/literal/eob next
	j = this.need;

	while(k<(j)){
	  if(n!=0)r=Z_OK;
	  else{

	    s.bitb=b;s.bitk=k;
	    z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	    s.write=q;
	    return s.inflate_flush(z,r);
	  }
	  n--;
	  b|=(z.next_in[p++]&0xff)<<k;
	  k+=8;
	}

	tindex=(this.tree_index+(b&inflate_mask[j]))*3;

	b>>>=(this.tree[tindex+1]);
	k-=(this.tree[tindex+1]);

	e=this.tree[tindex];

	if(e == 0){               // literal
	  this.lit = this.tree[tindex+2];
	  this.mode = IC_LIT;
	  break;
	}
	if((e & 16)!=0 ){          // length
	  this.get = e & 15;
	  this.len = this.tree[tindex+2];
	  this.mode = IC_LENEXT;
	  break;
	}
	if ((e & 64) == 0){        // next table
	  this.need = e;
	  this.tree_index = tindex/3 + this.tree[tindex+2];
	  break;
	}
	if ((e & 32)!=0){               // end of block
	  this.mode = IC_WASH;
	  break;
	}
	this.mode = IC_BADCODE;        // invalid code
	z.msg = "invalid literal/length code";
	r = Z_DATA_ERROR;

	s.bitb=b;s.bitk=k;
	z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	s.write=q;
	return s.inflate_flush(z,r);

      case IC_LENEXT:        // i: getting length extra (have base)
	j = this.get;

	while(k<(j)){
	  if(n!=0)r=Z_OK;
	  else{

	    s.bitb=b;s.bitk=k;
	    z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	    s.write=q;
	    return s.inflate_flush(z,r);
	  }
	  n--; b|=(z.next_in[p++]&0xff)<<k;
	  k+=8;
	}

	this.len += (b & inflate_mask[j]);

	b>>=j;
	k-=j;

	this.need = this.dbits;
	this.tree = this.dtree;
	this.tree_index = this.dtree_index;
	this.mode = IC_DIST;
      case IC_DIST:          // i: get distance next
	j = this.need;

	while(k<(j)){
	  if(n!=0)r=Z_OK;
	  else{

	    s.bitb=b;s.bitk=k;
	    z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	    s.write=q;
	    return s.inflate_flush(z,r);
	  }
	  n--; b|=(z.next_in[p++]&0xff)<<k;
	  k+=8;
	}

	tindex=(this.tree_index+(b & inflate_mask[j]))*3;

	b>>=this.tree[tindex+1];
	k-=this.tree[tindex+1];

	e = (this.tree[tindex]);
	if((e & 16)!=0){               // distance
	  this.get = e & 15;
	  this.dist = this.tree[tindex+2];
	  this.mode = IC_DISTEXT;
	  break;
	}
	if ((e & 64) == 0){        // next table
	  this.need = e;
	  this.tree_index = tindex/3 + this.tree[tindex+2];
	  break;
	}
	this.mode = IC_BADCODE;        // invalid code
	z.msg = "invalid distance code";
	r = Z_DATA_ERROR;

	s.bitb=b;s.bitk=k;
	z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	s.write=q;
	return s.inflate_flush(z,r);

      case IC_DISTEXT:       // i: getting distance extra
	j = this.get;

	while(k<(j)){
	  if(n!=0)r=Z_OK;
	  else{

	    s.bitb=b;s.bitk=k;
	    z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	    s.write=q;
	    return s.inflate_flush(z,r);
	  }
	  n--; b|=(z.next_in[p++]&0xff)<<k;
	  k+=8;
	}

	this.dist += (b & inflate_mask[j]);

	b>>=j;
	k-=j;

	this.mode = IC_COPY;
      case IC_COPY:          // o: copying bytes in window, waiting for space
        f = q - this.dist;
        while(f < 0){     // modulo window size-"while" instead
          f += s.end;     // of "if" handles invalid distances
	}
	while (this.len!=0){

	  if(m==0){
	    if(q==s.end&&s.read!=0){q=0;m=q<s.read?s.read-q-1:s.end-q;}
	    if(m==0){
	      s.write=q; r=s.inflate_flush(z,r);
	      q=s.write;m=q<s.read?s.read-q-1:s.end-q;

	      if(q==s.end&&s.read!=0){q=0;m=q<s.read?s.read-q-1:s.end-q;}

	      if(m==0){
		s.bitb=b;s.bitk=k;
		z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
		s.write=q;
		return s.inflate_flush(z,r);
	      }  
	    }
	  }

	  s.window[q++]=s.window[f++]; m--;

	  if (f == s.end)
            f = 0;
	  this.len--;
	}
	this.mode = IC_START;
	break;
      case IC_LIT:           // o: got literal, waiting for output space
	if(m==0){
	  if(q==s.end&&s.read!=0){q=0;m=q<s.read?s.read-q-1:s.end-q;}
	  if(m==0){
	    s.write=q; r=s.inflate_flush(z,r);
	    q=s.write;m=q<s.read?s.read-q-1:s.end-q;

	    if(q==s.end&&s.read!=0){q=0;m=q<s.read?s.read-q-1:s.end-q;}
	    if(m==0){
	      s.bitb=b;s.bitk=k;
	      z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	      s.write=q;
	      return s.inflate_flush(z,r);
	    }
	  }
	}
	r=Z_OK;

	s.window[q++]=this.lit; m--;

	this.mode = IC_START;
	break;
      case IC_WASH:           // o: got eob, possibly more output
	if (k > 7){        // return unused byte, if any
	  k -= 8;
	  n++;
	  p--;             // can always return one
	}

	s.write=q; r=s.inflate_flush(z,r);
	q=s.write;m=q<s.read?s.read-q-1:s.end-q;

	if (s.read != s.write){
	  s.bitb=b;s.bitk=k;
	  z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	  s.write=q;
	  return s.inflate_flush(z,r);
	}
	this.mode = IC_END;
      case IC_END:
	r = Z_STREAM_END;
	s.bitb=b;s.bitk=k;
	z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	s.write=q;
	return s.inflate_flush(z,r);

      case IC_BADCODE:       // x: got error

	r = Z_DATA_ERROR;

	s.bitb=b;s.bitk=k;
	z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	s.write=q;
	return s.inflate_flush(z,r);

      default:
	r = Z_STREAM_ERROR;

	s.bitb=b;s.bitk=k;
	z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	s.write=q;
	return s.inflate_flush(z,r);
      }
    }
  }

InfCodes.prototype.free = function(z){
    //  ZFREE(z, c);
}

  // Called with number of bytes left to write in window at least 258
  // (the maximum string length) and number of input bytes available
  // at least ten.  The ten bytes are six bytes for the longest length/
  // distance pair plus four bytes for overloading the bit buffer.

InfCodes.prototype.inflate_fast = function(bl, bd, tl, tl_index, td, td_index, s, z) {
    var t;                // temporary pointer
    var   tp;             // temporary pointer (int[])
    var tp_index;         // temporary pointer
    var e;                // extra bits or operation
    var b;                // bit buffer
    var k;                // bits in bit buffer
    var p;                // input data pointer
    var n;                // bytes available there
    var q;                // output window write pointer
    var m;                // bytes to end of window or read pointer
    var ml;               // mask for literal/length tree
    var md;               // mask for distance tree
    var c;                // bytes to copy
    var d;                // distance back to copy from
    var r;                // copy source pointer

    var tp_index_t_3;     // (tp_index+t)*3

    // load input, output, bit values
    p=z.next_in_index;n=z.avail_in;b=s.bitb;k=s.bitk;
    q=s.write;m=q<s.read?s.read-q-1:s.end-q;

    // initialize masks
    ml = inflate_mask[bl];
    md = inflate_mask[bd];

    // do until not enough input or output space for fast loop
    do {                          // assume called with m >= 258 && n >= 10
      // get literal/length code
      while(k<(20)){              // max bits for literal/length code
	n--;
	b|=(z.next_in[p++]&0xff)<<k;k+=8;
      }

      t= b&ml;
      tp=tl; 
      tp_index=tl_index;
      tp_index_t_3=(tp_index+t)*3;
      if ((e = tp[tp_index_t_3]) == 0){
	b>>=(tp[tp_index_t_3+1]); k-=(tp[tp_index_t_3+1]);

	s.window[q++] = tp[tp_index_t_3+2];
	m--;
	continue;
      }
      do {

	b>>=(tp[tp_index_t_3+1]); k-=(tp[tp_index_t_3+1]);

	if((e&16)!=0){
	  e &= 15;
	  c = tp[tp_index_t_3+2] + (b & inflate_mask[e]);

	  b>>=e; k-=e;

	  // decode distance base of block to copy
	  while(k<(15)){           // max bits for distance code
	    n--;
	    b|=(z.next_in[p++]&0xff)<<k;k+=8;
	  }

	  t= b&md;
	  tp=td;
	  tp_index=td_index;
          tp_index_t_3=(tp_index+t)*3;
	  e = tp[tp_index_t_3];

	  do {

	    b>>=(tp[tp_index_t_3+1]); k-=(tp[tp_index_t_3+1]);

	    if((e&16)!=0){
	      // get extra bits to add to distance base
	      e &= 15;
	      while(k<(e)){         // get extra bits (up to 13)
		n--;
		b|=(z.next_in[p++]&0xff)<<k;k+=8;
	      }

	      d = tp[tp_index_t_3+2] + (b&inflate_mask[e]);

	      b>>=(e); k-=(e);

	      // do the copy
	      m -= c;
	      if (q >= d){                // offset before dest
		//  just copy
		r=q-d;
		if(q-r>0 && 2>(q-r)){           
		  s.window[q++]=s.window[r++]; // minimum count is three,
		  s.window[q++]=s.window[r++]; // so unroll loop a little
		  c-=2;
		}
		else{
		  s.window[q++]=s.window[r++]; // minimum count is three,
		  s.window[q++]=s.window[r++]; // so unroll loop a little
		  c-=2;
		}
	      }
	      else{                  // else offset after destination
                r=q-d;
                do{
                  r+=s.end;          // force pointer in window
                }while(r<0);         // covers invalid distances
		e=s.end-r;
		if(c>e){             // if source crosses,
		  c-=e;              // wrapped copy
		  if(q-r>0 && e>(q-r)){           
		    do{s.window[q++] = s.window[r++];}
		    while(--e!=0);
		  }
		  else{
		    arrayCopy(s.window, r, s.window, q, e);
		    q+=e; r+=e; e=0;
		  }
		  r = 0;                  // copy rest from start of window
		}

	      }

	      // copy all or what's left
              do{s.window[q++] = s.window[r++];}
		while(--c!=0);
	      break;
	    }
	    else if((e&64)==0){
	      t+=tp[tp_index_t_3+2];
	      t+=(b&inflate_mask[e]);
	      tp_index_t_3=(tp_index+t)*3;
	      e=tp[tp_index_t_3];
	    }
	    else{
	      z.msg = "invalid distance code";

	      c=z.avail_in-n;c=(k>>3)<c?k>>3:c;n+=c;p-=c;k-=c<<3;

	      s.bitb=b;s.bitk=k;
	      z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	      s.write=q;

	      return Z_DATA_ERROR;
	    }
	  }
	  while(true);
	  break;
	}

	if((e&64)==0){
	  t+=tp[tp_index_t_3+2];
	  t+=(b&inflate_mask[e]);
	  tp_index_t_3=(tp_index+t)*3;
	  if((e=tp[tp_index_t_3])==0){

	    b>>=(tp[tp_index_t_3+1]); k-=(tp[tp_index_t_3+1]);

	    s.window[q++]=tp[tp_index_t_3+2];
	    m--;
	    break;
	  }
	}
	else if((e&32)!=0){

	  c=z.avail_in-n;c=(k>>3)<c?k>>3:c;n+=c;p-=c;k-=c<<3;
 
	  s.bitb=b;s.bitk=k;
	  z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	  s.write=q;

	  return Z_STREAM_END;
	}
	else{
	  z.msg="invalid literal/length code";

	  c=z.avail_in-n;c=(k>>3)<c?k>>3:c;n+=c;p-=c;k-=c<<3;

	  s.bitb=b;s.bitk=k;
	  z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
	  s.write=q;

	  return Z_DATA_ERROR;
	}
      } 
      while(true);
    } 
    while(m>=258 && n>= 10);

    // not enough input or output--restore pointers and return
    c=z.avail_in-n;c=(k>>3)<c?k>>3:c;n+=c;p-=c;k-=c<<3;

    s.bitb=b;s.bitk=k;
    z.avail_in=n;z.total_in+=p-z.next_in_index;z.next_in_index=p;
    s.write=q;

    return Z_OK;
}

//
// InfTree.java
//

function InfTree() {
}

InfTree.prototype.huft_build = function(b, bindex, n, s, d, e, t, m, hp, hn, v) {

    // Given a list of code lengths and a maximum table size, make a set of
    // tables to decode that set of codes.  Return Z_OK on success, Z_BUF_ERROR
    // if the given code set is incomplete (the tables are still built in this
    // case), Z_DATA_ERROR if the input is invalid (an over-subscribed set of
    // lengths), or Z_MEM_ERROR if not enough memory.

    var a;                       // counter for codes of length k
    var f;                       // i repeats in table every f entries
    var g;                       // maximum code length
    var h;                       // table level
    var i;                       // counter, current code
    var j;                       // counter
    var k;                       // number of bits in current code
    var l;                       // bits per table (returned in m)
    var mask;                    // (1 << w) - 1, to avoid cc -O bug on HP
    var p;                       // pointer into c[], b[], or v[]
    var q;                       // points to current table
    var w;                       // bits before this table == (l * h)
    var xp;                      // pointer into x
    var y;                       // number of dummy codes added
    var z;                       // number of entries in current table

    // Generate counts for each bit length

    p = 0; i = n;
    do {
      this.c[b[bindex+p]]++; p++; i--;   // assume all entries <= BMAX
    }while(i!=0);

    if(this.c[0] == n){                // null input--all zero length codes
      t[0] = -1;
      m[0] = 0;
      return Z_OK;
    }

    // Find minimum and maximum length, bound *m by those
    l = m[0];
    for (j = 1; j <= BMAX; j++)
      if(this.c[j]!=0) break;
    k = j;                        // minimum code length
    if(l < j){
      l = j;
    }
    for (i = BMAX; i!=0; i--){
      if(this.c[i]!=0) break;
    }
    g = i;                        // maximum code length
    if(l > i){
      l = i;
    }
    m[0] = l;

    // Adjust last length count to fill out codes, if needed
    for (y = 1 << j; j < i; j++, y <<= 1){
      if ((y -= this.c[j]) < 0){
        return Z_DATA_ERROR;
      }
    }
    if ((y -= this.c[i]) < 0){
      return Z_DATA_ERROR;
    }
    this.c[i] += y;

    // Generate starting offsets into the value table for each length
    this.x[1] = j = 0;
    p = 1;  xp = 2;
    while (--i!=0) {                 // note that i == g from above
      this.x[xp] = (j += this.c[p]);
      xp++;
      p++;
    }

    // Make a table of values in order of bit lengths
    i = 0; p = 0;
    do {
      if ((j = b[bindex+p]) != 0){
        this.v[this.x[j]++] = i;
      }
      p++;
    }
    while (++i < n);
    n = this.x[g];                     // set n to length of v

    // Generate the Huffman codes and for each, make the table entries
    this.x[0] = i = 0;                 // first Huffman code is zero
    p = 0;                        // grab values in bit order
    h = -1;                       // no tables yet--level -1
    w = -l;                       // bits decoded == (l * h)
    this.u[0] = 0;                     // just to keep compilers happy
    q = 0;                        // ditto
    z = 0;                        // ditto

    // go through the bit lengths (k already is bits in shortest code)
    for (; k <= g; k++){
      a = this.c[k];
      while (a--!=0){
	// here i is the Huffman code of length k bits for value *p
	// make tables up to required level
        while (k > w + l){
          h++;
          w += l;                 // previous table always l bits
	  // compute minimum size table less than or equal to l bits
          z = g - w;
          z = (z > l) ? l : z;        // table size upper limit
          if((f=1<<(j=k-w))>a+1){     // try a k-w bit table
                                      // too few codes for k-w bit table
            f -= a + 1;               // deduct codes from patterns left
            xp = k;
            if(j < z){
              while (++j < z){        // try smaller tables up to z bits
                if((f <<= 1) <= this.c[++xp])
                  break;              // enough codes to use up j bits
                f -= this.c[xp];           // else deduct codes from patterns
              }
	    }
          }
          z = 1 << j;                 // table entries for j-bit table

	  // allocate new table
          if (this.hn[0] + z > MANY){       // (note: doesn't matter for fixed)
            return Z_DATA_ERROR;       // overflow of MANY
          }
          this.u[h] = q = /*hp+*/ this.hn[0];   // DEBUG
          this.hn[0] += z;
 
	  // connect to last table, if there is one
	  if(h!=0){
            this.x[h]=i;           // save pattern for backing up
            this.r[0]=j;     // bits in this table
            this.r[1]=l;     // bits to dump before this table
            j=i>>>(w - l);
            this.r[2] = (q - this.u[h-1] - j);               // offset to this table
            arrayCopy(this.r, 0, hp, (this.u[h-1]+j)*3, 3); // connect to last table
          }
          else{
            t[0] = q;               // first table is returned result
	  }
        }

	// set up table entry in r
        this.r[1] = (k - w);
        if (p >= n){
          this.r[0] = 128 + 64;      // out of values--invalid code
	}
        else if (v[p] < s){
          this.r[0] = (this.v[p] < 256 ? 0 : 32 + 64);  // 256 is end-of-block
          this.r[2] = this.v[p++];          // simple code is just the value
        }
        else{
          this.r[0]=(e[this.v[p]-s]+16+64); // non-simple--look up in lists
          this.r[2]=d[this.v[p++] - s];
        }

        // fill code-like entries with r
        f=1<<(k-w);
        for (j=i>>>w;j<z;j+=f){
          arrayCopy(this.r, 0, hp, (q+j)*3, 3);
	}

	// backwards increment the k-bit code i
        for (j = 1 << (k - 1); (i & j)!=0; j >>>= 1){
          i ^= j;
	}
        i ^= j;

	// backup over finished tables
        mask = (1 << w) - 1;      // needed on HP, cc -O bug
        while ((i & mask) != this.x[h]){
          h--;                    // don't need to update q
          w -= l;
          mask = (1 << w) - 1;
        }
      }
    }
    // Return Z_BUF_ERROR if we were given an incomplete table
    return y != 0 && g != 1 ? Z_BUF_ERROR : Z_OK;
}

InfTree.prototype.inflate_trees_bits = function(c, bb, tb, hp, z) {
    var result;
    this.initWorkArea(19);
    this.hn[0]=0;
    result = this.huft_build(c, 0, 19, 19, null, null, tb, bb, hp, this.hn, this.v);

    if(result == Z_DATA_ERROR){
      z.msg = "oversubscribed dynamic bit lengths tree";
    }
    else if(result == Z_BUF_ERROR || bb[0] == 0){
      z.msg = "incomplete dynamic bit lengths tree";
      result = Z_DATA_ERROR;
    }
    return result;
}

InfTree.prototype.inflate_trees_dynamic = function(nl, nd, c, bl, bd, tl, td, hp, z) {
    var result;

    // build literal/length tree
    this.initWorkArea(288);
    this.hn[0]=0;
    result = this.huft_build(c, 0, nl, 257, cplens, cplext, tl, bl, hp, this.hn, this.v);
    if (result != Z_OK || bl[0] == 0){
      if(result == Z_DATA_ERROR){
        z.msg = "oversubscribed literal/length tree";
      }
      else if (result != Z_MEM_ERROR){
        z.msg = "incomplete literal/length tree";
        result = Z_DATA_ERROR;
      }
      return result;
    }

    // build distance tree
    this.initWorkArea(288);
    result = this.huft_build(c, nl, nd, 0, cpdist, cpdext, td, bd, hp, this.hn, this.v);

    if (result != Z_OK || (bd[0] == 0 && nl > 257)){
      if (result == Z_DATA_ERROR){
        z.msg = "oversubscribed distance tree";
      }
      else if (result == Z_BUF_ERROR) {
        z.msg = "incomplete distance tree";
        result = Z_DATA_ERROR;
      }
      else if (result != Z_MEM_ERROR){
        z.msg = "empty distance tree with lengths";
        result = Z_DATA_ERROR;
      }
      return result;
    }

    return Z_OK;
}
/*
  static int inflate_trees_fixed(int[] bl,  //literal desired/actual bit depth
                                 int[] bd,  //distance desired/actual bit depth
                                 int[][] tl,//literal/length tree result
                                 int[][] td,//distance tree result 
                                 ZStream z  //for memory allocation
				 ){

*/

function inflate_trees_fixed(bl, bd, tl, td, z) {
    bl[0]=fixed_bl;
    bd[0]=fixed_bd;
    tl[0]=fixed_tl;
    td[0]=fixed_td;
    return Z_OK;
}

InfTree.prototype.initWorkArea = function(vsize){
    if(this.hn==null){
        this.hn=new Int32Array(1);
        this.v=new Int32Array(vsize);
        this.c=new Int32Array(BMAX+1);
        this.r=new Int32Array(3);
        this.u=new Int32Array(BMAX);
        this.x=new Int32Array(BMAX+1);
    }
    if(this.v.length<vsize){ 
        this.v=new Int32Array(vsize); 
    }
    for(var i=0; i<vsize; i++){this.v[i]=0;}
    for(var i=0; i<BMAX+1; i++){this.c[i]=0;}
    for(var i=0; i<3; i++){this.r[i]=0;}
//  for(int i=0; i<BMAX; i++){u[i]=0;}
    arrayCopy(this.c, 0, this.u, 0, BMAX);
//  for(int i=0; i<BMAX+1; i++){x[i]=0;}
    arrayCopy(this.c, 0, this.x, 0, BMAX+1);
}

var testArray = new Uint8Array(1);
var hasSubarray = (typeof testArray.subarray === 'function');
var hasSlice = false; /* (typeof testArray.slice === 'function'); */ // Chrome slice performance is so dire that we're currently not using it...

function arrayCopy(src, srcOffset, dest, destOffset, count) {
    if (count == 0) {
        return;
    } 
    if (!src) {
        throw "Undef src";
    } else if (!dest) {
        throw "Undef dest";
    }

    if (srcOffset == 0 && count == src.length) {
        arrayCopy_fast(src, dest, destOffset);
    } else if (hasSubarray) {
        arrayCopy_fast(src.subarray(srcOffset, srcOffset + count), dest, destOffset); 
    } else if (src.BYTES_PER_ELEMENT == 1 && count > 100) {
        arrayCopy_fast(new Uint8Array(src.buffer, src.byteOffset + srcOffset, count), dest, destOffset);
    } else { 
        arrayCopy_slow(src, srcOffset, dest, destOffset, count);
    }

}

function arrayCopy_slow(src, srcOffset, dest, destOffset, count) {

    // dlog('_slow call: srcOffset=' + srcOffset + '; destOffset=' + destOffset + '; count=' + count);

     for (var i = 0; i < count; ++i) {
        dest[destOffset + i] = src[srcOffset + i];
    }
}

function arrayCopy_fast(src, dest, destOffset) {
    dest.set(src, destOffset);
}


  // largest prime smaller than 65536
var ADLER_BASE=65521; 
  // NMAX is the largest n such that 255n(n+1)/2 + (n+1)(BASE-1) <= 2^32-1
var ADLER_NMAX=5552;

function adler32(adler, /* byte[] */ buf,  index, len){
    if(buf == null){ return 1; }

    var s1=adler&0xffff;
    var s2=(adler>>16)&0xffff;
    var k;

    while(len > 0) {
      k=len<ADLER_NMAX?len:ADLER_NMAX;
      len-=k;
      while(k>=16){
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        s1+=buf[index++]&0xff; s2+=s1;
        k-=16;
      }
      if(k!=0){
        do{
          s1+=buf[index++]&0xff; s2+=s1;
        }
        while(--k!=0);
      }
      s1%=ADLER_BASE;
      s2%=ADLER_BASE;
    }
    return (s2<<16)|s1;
}



function jszlib_inflate_buffer(buffer, start, length, afterUncOffset) {
    if (!start) {
        buffer = new Uint8Array(buffer);
    } else {
        buffer = new Uint8Array(buffer, start, length);
    }

    var z = new ZStream();
    z.inflateInit(DEF_WBITS, true);
    z.next_in = buffer;
    z.next_in_index = 0;
    z.avail_in = buffer.length;

    var oBlockList = [];
    var totalSize = 0;
    while (true) {
        var obuf = new Uint8Array(32000);
        z.next_out = obuf;
        z.next_out_index = 0;
        z.avail_out = obuf.length;
        var status = z.inflate(Z_NO_FLUSH);
        if (status != Z_OK && status != Z_STREAM_END) {
            throw z.msg;
        }
        if (z.avail_out != 0) {
            var newob = new Uint8Array(obuf.length - z.avail_out);
            arrayCopy(obuf, 0, newob, 0, (obuf.length - z.avail_out));
            obuf = newob;
        }
        oBlockList.push(obuf);
        totalSize += obuf.length;
        if (status == Z_STREAM_END) {
            break;
        }
    }

    if (afterUncOffset) {
        afterUncOffset[0] = (start || 0) + z.next_in_index;
    }

    if (oBlockList.length == 1) {
        return oBlockList[0].buffer;
    } else {
        var out = new Uint8Array(totalSize);
        var cursor = 0;
        for (var i = 0; i < oBlockList.length; ++i) {
            var b = oBlockList[i];
            arrayCopy(b, 0, out, cursor, b.length);
            cursor += b.length;
        }
        return out.buffer;
    }
}/*
html5slider - a JS implementation of <input type=range> for Firefox 16 and up
https://github.com/fryn/html5slider

Copyright (c) 2010-2012 Frank Yan, <http://frankyan.com>

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/

(function() {

// test for native support
var test = document.createElement('input');
try {
  test.type = 'range';
  if (test.type == 'range')
    return;
} catch (e) {
  return;
}

// test for required property support
test.style.background = 'linear-gradient(red, red)';
if (!test.style.backgroundImage || !('MozAppearance' in test.style) ||
    !document.mozSetImageElement || !this.MutationObserver)
  return;

var scale;
var isMac = navigator.platform == 'MacIntel';
var thumb = {
  radius: isMac ? 9 : 6,
  width: isMac ? 22 : 12,
  height: isMac ? 16 : 20
};
var track = 'linear-gradient(transparent ' + (isMac ?
  '6px, #999 6px, #999 7px, #ccc 8px, #bbb 9px, #bbb 10px, transparent 10px' :
  '9px, #999 9px, #bbb 10px, #fff 11px, transparent 11px') +
  ', transparent)';
var styles = {
  'min-width': thumb.width + 'px',
  'min-height': thumb.height + 'px',
  'max-height': thumb.height + 'px',
  padding: '0 0 ' + (isMac ? '2px' : '1px'),
  border: 0,
  'border-radius': 0,
  cursor: 'default',
  'text-indent': '-999999px' // -moz-user-select: none; breaks mouse capture
};
var options = {
  attributes: true,
  attributeFilter: ['min', 'max', 'step', 'value']
};
var forEach = Array.prototype.forEach;
var onChange = document.createEvent('HTMLEvents');
onChange.initEvent('change', true, false);

if (document.readyState == 'loading')
  document.addEventListener('DOMContentLoaded', initialize, true);
else
  initialize();

function initialize() {
  // create initial sliders
  forEach.call(document.querySelectorAll('input[type=range]'), transform);
  // create sliders on-the-fly
  new MutationObserver(function(mutations) {
    mutations.forEach(function(mutation) {
      if (mutation.addedNodes)
        forEach.call(mutation.addedNodes, function(node) {
          check(node);
          if (node.childElementCount)
            forEach.call(node.querySelectorAll('input'), check);
        });
    });
  }).observe(document, { childList: true, subtree: true });
}

function check(input) {
  if (input.localName == 'input' && input.type != 'range' &&
      input.getAttribute('type') == 'range')
    transform(input);
}

function transform(slider) {

  var isValueSet, areAttrsSet, isChanged, isClick, prevValue, rawValue, prevX;
  var min, max, step, range, value = slider.value;

  // lazily create shared slider affordance
  if (!scale) {
    scale = document.body.appendChild(document.createElement('hr'));
    style(scale, {
      '-moz-appearance': isMac ? 'scale-horizontal' : 'scalethumb-horizontal',
      display: 'block',
      visibility: 'visible',
      opacity: 1,
      position: 'fixed',
      top: '-999999px'
    });
    document.mozSetImageElement('__sliderthumb__', scale);
  }

  // reimplement value and type properties
  var getValue = function() { return '' + value; };
  var setValue = function setValue(val) {
    value = '' + val;
    isValueSet = true;
    draw();
    delete slider.value;
    slider.value = value;
    slider.__defineGetter__('value', getValue);
    slider.__defineSetter__('value', setValue);
  };
  slider.__defineGetter__('value', getValue);
  slider.__defineSetter__('value', setValue);
  slider.__defineGetter__('type', function() { return 'range'; });

  // sync properties with attributes
  ['min', 'max', 'step'].forEach(function(prop) {
    if (slider.hasAttribute(prop))
      areAttrsSet = true;
    slider.__defineGetter__(prop, function() {
      return this.hasAttribute(prop) ? this.getAttribute(prop) : '';
    });
    slider.__defineSetter__(prop, function(val) {
      val === null ? this.removeAttribute(prop) : this.setAttribute(prop, val);
    });
  });

  // initialize slider
  slider.readOnly = true;
  style(slider, styles);
  update();

  new MutationObserver(function(mutations) {
    mutations.forEach(function(mutation) {
      if (mutation.attributeName != 'value') {
        update();
        areAttrsSet = true;
      }
      // note that value attribute only sets initial value
      else if (!isValueSet) {
        value = slider.getAttribute('value');
        draw();
      }
    });
  }).observe(slider, options);

  slider.addEventListener('mousedown', onDragStart, true);
  slider.addEventListener('keydown', onKeyDown, true);
  slider.addEventListener('focus', onFocus, true);
  slider.addEventListener('blur', onBlur, true);

  function onDragStart(e) {
    isClick = true;
    setTimeout(function() { isClick = false; }, 0);
    if (e.button || !range)
      return;
    var width = parseFloat(getComputedStyle(this, 0).width);
    var multiplier = (width - thumb.width) / range;
    if (!multiplier)
      return;
    // distance between click and center of thumb
    var dev = e.clientX - this.getBoundingClientRect().left - thumb.width / 2 -
              (value - min) * multiplier;
    // if click was not on thumb, move thumb to click location
    if (Math.abs(dev) > thumb.radius) {
      isChanged = true;
      this.value -= -dev / multiplier;
    }
    rawValue = value;
    prevX = e.clientX;
    this.addEventListener('mousemove', onDrag, true);
    this.addEventListener('mouseup', onDragEnd, true);
  }

  function onDrag(e) {
    var width = parseFloat(getComputedStyle(this, 0).width);
    var multiplier = (width - thumb.width) / range;
    if (!multiplier)
      return;
    rawValue += (e.clientX - prevX) / multiplier;
    prevX = e.clientX;
    isChanged = true;
    this.value = rawValue;
  }

  function onDragEnd() {
    this.removeEventListener('mousemove', onDrag, true);
    this.removeEventListener('mouseup', onDragEnd, true);
  }

  function onKeyDown(e) {
    if (e.keyCode > 36 && e.keyCode < 41) { // 37-40: left, up, right, down
      onFocus.call(this);
      isChanged = true;
      this.value = value + (e.keyCode == 38 || e.keyCode == 39 ? step : -step);
    }
  }

  function onFocus() {
    if (!isClick)
      this.style.boxShadow = !isMac ? '0 0 0 2px #fb0' :
        'inset 0 0 20px rgba(0,127,255,.1), 0 0 1px rgba(0,127,255,.4)';
  }

  function onBlur() {
    this.style.boxShadow = '';
  }

  // determines whether value is valid number in attribute form
  function isAttrNum(value) {
    return !isNaN(value) && +value == parseFloat(value);
  }

  // validates min, max, and step attributes and redraws
  function update() {
    min = isAttrNum(slider.min) ? +slider.min : 0;
    max = isAttrNum(slider.max) ? +slider.max : 100;
    if (max < min)
      max = min > 100 ? min : 100;
    step = isAttrNum(slider.step) && slider.step > 0 ? +slider.step : 1;
    range = max - min;
    draw(true);
  }

  // recalculates value property
  function calc() {
    if (!isValueSet && !areAttrsSet)
      value = slider.getAttribute('value');
    if (!isAttrNum(value))
      value = (min + max) / 2;;
    // snap to step intervals (WebKit sometimes does not - bug?)
    value = Math.round((value - min) / step) * step + min;
    if (value < min)
      value = min;
    else if (value > max)
      value = min + ~~(range / step) * step;
  }

  // renders slider using CSS background ;)
  function draw(attrsModified) {
    calc();
    if (isChanged && value != prevValue)
      slider.dispatchEvent(onChange);
    isChanged = false;
    if (!attrsModified && value == prevValue)
      return;
    prevValue = value;
    var position = range ? (value - min) / range * 100 : 0;
    var bg = '-moz-element(#__sliderthumb__) ' + position + '% no-repeat, ';
    style(slider, { background: bg + track });
  }

}

function style(element, styles) {
  for (var prop in styles)
    element.style.setProperty(prop, styles[prop], 'important');
}

})();
