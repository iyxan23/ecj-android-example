package com.iyxan23.ecjexample

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class ECJFragment : Fragment() {

    private lateinit var viewModel: ECJViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.ecj_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        output = view.findViewById(R.id.textView2)
        args = view.findViewById(R.id.editTextTextPersonName)
        execute = view.findViewById(R.id.button)
    }

    private lateinit var output: TextView
    private lateinit var args: EditText
    private lateinit var execute: Button

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel = ViewModelProvider(this).get(ECJViewModel::class.java)

        viewModel.checkAndExtractJars(requireContext())
        viewModel.ecjOutput.observe(viewLifecycleOwner) { output.text = it }

        execute.setOnClickListener { viewModel.runCommand(args.text.toString()) }
    }
}