using System.IO;

namespace Microsoft.DotNet.Archive
{
    internal static class PathUtils
    {
        internal static string Normalize(string path) => path?.Replace(Path.DirectorySeparatorChar, '/');
    }
}
