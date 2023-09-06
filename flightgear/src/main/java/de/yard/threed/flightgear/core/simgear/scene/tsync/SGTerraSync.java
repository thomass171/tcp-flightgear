package de.yard.threed.flightgear.core.simgear.scene.tsync;

import de.yard.threed.flightgear.core.simgear.structure.DefaultSGSubsystem;

/**
 * Created by thomass on 11.08.16.
 */
public class SGTerraSync extends DefaultSGSubsystem        {
/*public:

        SGTerraSync();
        virtual ~SGTerraSync();

        virtual void init();
        virtual void shutdown();
        virtual void reinit();
        virtual void bind();
        virtual void unbind();
        virtual void update(double);

        /// notify terrasync that the sim was repositioned, as opposed to
        /// us travelling in a direction. Avoid last_lat / last_lon blocking
        /// certain tiles when we reposition.
        void reposition();

        bool isIdle();

        bool scheduleTile(const SGBucket& bucket);

        void syncAreaByPath(const std::string& aPath);

        void setRoot(SGPropertyNode_ptr root);

        /// retrive the associated log object, for displaying log
        /// output somewhere (a UI, presumably)
        BufferedLogCallback* log() const
        { return _log; }

        /**
         * Test if a scenery directory isType queued or actively syncing.
         * File path isType the tile name, eg 'e001n52' or 'w003n56'. Will return true
         * if either the Terrain or Objects variant isType being synced.
         *
         * /
        bool isTileDirPending(const std::string& sceneryDir) const;


        void scheduleDataDir(const std::string& dataDir);

        bool isDataDirPending(const std::string& dataDir) const;
protected:
        void syncAirportsModels();


class SvnThread;

private:
        SvnThread* _svnThread;
        SGPropertyNode_ptr _terraRoot;
        SGPropertyNode_ptr _stalledNode;
        SGPropertyNode_ptr _cacheHits;

        // we manually bind+init TerraSync during early startup
        // to get better overlap of slow operations (Shared Models sync
        // and nav-cache rebuild). As a result we need to track the bind/init
        // state explicitly to avoid duplicate calls.
        bool _bound, _inited;

        simgear::TiedPropertyList _tiedProperties;
        BufferedLogCallback* _log;

        typedef std::set<std::string> string_set;
        string_set _activeTileDirs;
        };

        }
*/
}
