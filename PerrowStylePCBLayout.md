# Perrow Style PCB Layout #

I've had requests for a tutorial about how I use DIYLC to create my PCBs, let's start with an example. This is the Small Clone Chorus, I started with the layout from <a target="_blank" href="http://www.tonepad.com/project.asp?id=8" rel="external">Tonepad </a>, but squeezed it a little to make it fit into a 1590B enclosure.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/dl16.png' /></p>

This layout uses all of the the techniques I consider to be part of my style, though I recently discovered that there's a place on that board where I missed making a heat relief in the ground plane/trace.

## Trace width ##
I've set the trace width to 1.3mm, mostly to save on etchant. This trace width also means that when grid spacing is set to 0.05 inch, two adjecent traces overlaps.

<strong>UPDATE:</strong> I've actually started setting trace width to 1.4mm, mostly so that everything looks nice in the interface. With 1.3mm traces and 100% zoom in DIYLC there are thin gaps between adjecent traces, with 1.4mm they're gone. Plus it saves just a little more etchant.

## Curved Traces ##
Lets start with the most basic part of my style, the curved trace, a truly versatile object.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/dl33.png' /></p>

The most basic use for the curved trace is the rounded corner. Start by moving the second control point to the exact position of the first.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/dl31.png' /></p>

Then move the third control point to the grid point next to the first and second control points.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/dl32.png' /></p>

Finally move the last control point to the grid point below the third.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/dl34.png' /></p>

## Larger Diameter ##
Sometimes there's two tracks turning the same direction. The curved traces scales quite nicely to accomodate that.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/dl40.png' /></p>

These can be used together to make ground planes with filled corners.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/dl41.png' /></p>

## Heat Reliefs ##
I tend let the ground trace flow and fill all avaliable areas, moving things around to access more areas too if needed. But with that comes the need for heat reliefs, these makes it a little easier to solder components by allowing a smaller area to be heated instead of a large ground plane. We start once again with the curved trace but arrange the control points in a square.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/dl35.png' /></p>

Make another one that's the mirror image of that one and place them together.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/dl36.png' /></p>

To make the actual ground plane around it, add a couple of straight traces.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/dl37.png' /></p>

Add a few between, the vertical ones are just to block out a few pixels that the other traces just misses.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/dl38.png' /></p>

And this is the final result.

<p align='center'><img src='https://raw.githubusercontent.com/bancika/diy-layout-creator/wiki/images/dl39.png' /></p>

## Final words ##
There are definitely situations where the techniques covered here won't be enough to get me where I want to be with a circuit, but they easily cover over 90 percent of what I do. Looking at the Small Clone example above there's a place where I used thinner traces to fit an extra trace beneath an IC and one or more places where I've curved a few traces in a "non standard" way.

It does take slightly longer to do boards that's visually pleasing, but I do like to tinker with my circuits and do really think it's worth it.
