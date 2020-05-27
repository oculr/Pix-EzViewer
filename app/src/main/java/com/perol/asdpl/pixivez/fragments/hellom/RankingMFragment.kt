/*
 * MIT License
 *
 * Copyright (c) 2019 Perol_Notsfsssf
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE
 */

package com.perol.asdpl.pixivez.fragments.hellom


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.tabs.TabLayout
import com.perol.asdpl.pixivez.R
import com.perol.asdpl.pixivez.adapters.RankingAdapter
import com.perol.asdpl.pixivez.objects.AdapterRefreshEvent
import com.perol.asdpl.pixivez.objects.BaseFragment
import com.perol.asdpl.pixivez.viewmodel.RankingMViewModel
import com.perol.asdpl.pixivez.viewmodel.factory.RankingShareViewModel
import kotlinx.android.synthetic.main.fragment_ranking_m.*
import kotlinx.coroutines.runBlocking
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RankingMFragment.newInstance] factory method to
 * create an instance of this fragment.
 *
 */
class RankingMFragment : BaseFragment() {


    var picDate: String? = null
    var viewmodel: RankingMViewModel? = null
    var sharemodel: RankingShareViewModel? = null
    lateinit var rankingAdapter: RankingAdapter
    private var param1: String? = null
    private var param2: Int? = null
    override fun loadData() {

        viewmodel!!.first(param1!!, picDate)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: AdapterRefreshEvent) {
        runBlocking {
            val allTags = blockViewModel.getAllTags()
            blockTags = allTags.map {
                it.name
            }
            rankingAdapter.blockTags = blockTags
            rankingAdapter.notifyDataSetChanged()
        }
    }
    fun lazyLoad() {
        viewmodel = ViewModelProvider(this).get(RankingMViewModel::class.java)
        sharemodel = ViewModelProvider(requireActivity()).get(RankingShareViewModel::class.java)
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        picDate = if (sharemodel!!.picDateShare.value == "$year-$month-$day") {
            null
        } else {
            sharemodel!!.picDateShare.value
        }
        sharemodel!!.picDateShare.observe(this, Observer {

            viewmodel!!.datePick(param1!!, it)
        })
        viewmodel!!.addillusts.observe(this, Observer {
            if (it != null) {
                rankingAdapter.addData(it)
            }
        })
        viewmodel!!.illusts.observe(this, Observer {
            swiperefresh_rankingm.isRefreshing = false
            rankingAdapter.setNewData(it)
        })
        viewmodel!!.bookmarknum.observe(this, Observer {
            if (it != null) {
                viewmodel!!.onItemChildLongClick(it)
            }
        })
        viewmodel!!.nexturl.observe(this, Observer {
            if (it == null) {
                rankingAdapter.loadMoreEnd()
            } else {
                rankingAdapter.loadMoreComplete()
            }
        })
    }

    var exitTime = 0L

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        swiperefresh_rankingm.setOnRefreshListener {
            viewmodel!!.onRefresh(param1!!, picDate)
        }
        rankingAdapter.loadMoreModule?.setOnLoadMoreListener {
            viewmodel!!.onLoadMore()
        }
        recyclerview_rankingm.apply {
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = rankingAdapter
        }
        parentFragment?.view?.findViewById<TabLayout>(R.id.tablayout_rankingm)?.getTabAt(param2!!)
            ?.view?.setOnClickListener {
            if ((System.currentTimeMillis() - exitTime) > 3000) {

                exitTime = System.currentTimeMillis()
            } else {
                recyclerview_rankingm.smoothScrollToPosition(0)
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getInt(ARG_PARAM2)
        }
        lazyLoad()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rankingAdapter = RankingAdapter(
            R.layout.view_ranking_item,
            null,
            isR18on, blockTags
        )
        return inflater.inflate(R.layout.fragment_ranking_m, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RankingMFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, position: Int) =
            RankingMFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putInt(ARG_PARAM2, position)
                }
            }
    }
}
