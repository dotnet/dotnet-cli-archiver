// Copyright (c) .NET Foundation and contributors. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

using System;
using System.Diagnostics;

namespace Microsoft.DotNet.Archive
{
    public class ConsoleProgressReport : IProgress<ProgressReport>
    {
        private string _currentPhase;
        private int _lastLineLength = 0;
        private long _lastProgress = -1;
        private Stopwatch _stopwatch;
        private object _stateLock = new object();

        public void Report(ProgressReport value)
        {
            long progress = (long)(100 * ((double)value.Ticks / value.Total));

            lock (_stateLock)
            {
                if (progress == _lastProgress && value.Phase == _currentPhase)
                {
                    return;
                }

                if (value.Phase != _currentPhase)
                {
                    Console.Write(value.Phase);
                    Console.Write(" ");
                    _currentPhase = value.Phase;
                    _lastLineLength = 0;
                    _stopwatch = Stopwatch.StartNew();
                }

                if (Console.IsOutputRedirected)
                {
                    var delta = (progress / 10) - ((_lastProgress == -1 ? 0 : _lastProgress) / 10);
                    if (delta > 0)
                    {
                        Console.Write(new string('.', (int)delta));
                    }
                }
                else
                {
                    var percentage = $"{progress}%";
                    if (_lastLineLength > 0)
                    {
                        Console.Write(new string('\b', _lastLineLength));
                    }
                    Console.Write(percentage);
                    _lastLineLength = percentage.Length;
                }

                if (progress == 100)
                {
                    Console.WriteLine($" {_stopwatch.ElapsedMilliseconds} ms");
                }

                _lastProgress = progress;
            }
        }
    }
}
